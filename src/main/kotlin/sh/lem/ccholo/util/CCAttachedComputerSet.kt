/*
 * The following code is ported from CC: Tweaked 1.116.1's AttachedComputerSet.java, which is licensed under MPL-2.0.
 * The full terms of the license can be found in `LICENSES/MPL-2.0.txt`.
 *
 * This code is vendored here to reduce dependency on CC: Tweaked's core API, but more importantly, to allow
 * compatibility with CC: Tweaked 1.113.1.
 *
 * Modifications made for Kotlin porting.
 */
package sh.lem.ccholo.util

import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Consumer
import javax.annotation.Nullable

/**
 * A thread-safe collection of computers.
 * <p>
 * This collection is intended to be used by peripherals that need to maintain a set of all attached computers.
 * <p>
 * It is recommended to use over Java's built-in concurrent collections (e.g. {@link CopyOnWriteArraySet} or
 * {@link ConcurrentHashMap}), as {@link AttachedComputerSet} ensures that computers cannot be accessed after they are
 * detached, guaranteeing that {@link NotAttachedException}s will not be thrown.
 * <p>
 * To ensure this, {@link AttachedComputerSet} is not directly iterable, as we cannot ensure that computers are not
 * detached while the iterator is running (and so trying to use the computer would error). Instead, computers should be
 * looped over using {@link #forEach(Consumer)}.
 *
 * @author SquidDev
 * @see <https://github.com/cc-tweaked/CC-Tweaked/blob/b9ed669/projects/core-api/src/main/java/dan200/computercraft/api/peripheral/AttachedComputerSet.java>
 */
class CCAttachedComputerSet {
  private val lock = ReentrantReadWriteLock()
  private val computers: MutableSet<IComputerAccess> = HashSet(0)

  /**
   * Add a computer to this collection of computers. This should be called from
   * [IPeripheral.attach].
   *
   * @param computer The computer to add.
   */
  fun add(computer: IComputerAccess) {
    lock.writeLock().lock()
    try {
      computers.add(computer)
    } finally {
      lock.writeLock().unlock()
    }
  }

  /**
   * Remove a computer from this collection of computers. This should be called from
   * [IPeripheral.detach].
   *
   * @param computer The computer to remove.
   */
  fun remove(computer: IComputerAccess) {
    lock.writeLock().lock()
    try {
      computers.remove(computer)
    } finally {
      lock.writeLock().unlock()
    }
  }

  /**
   * Apply an action to each computer in this collection.
   *
   * @param action The action to apply.
   */
  fun forEach(action: Consumer<in IComputerAccess>) {
    lock.readLock().lock()
    try {
      computers.forEach(action)
    } finally {
      lock.readLock().unlock()
    }
  }

  /**
   * [Queue an event][IComputerAccess.queueEvent] on all computers.
   *
   * @param event     The name of the event to queue.
   * @param arguments The arguments for this event.
   * @see IComputerAccess.queueEvent
   */
  fun queueEvent(event: String, @Nullable vararg arguments: Any) {
    forEach { c -> c.queueEvent(event, arguments) }
  }

  /**
   * Determine if this collection contains any computers.
   *
   *
   * This method is primarily intended for presentation purposes (such as rendering an icon in the UI if a computer
   * is attached to your peripheral). Due to the multi-threaded nature of peripherals, it is not recommended to guard
   * any logic behind this check.
   *
   *
   * For instance, `if(computers.hasComputers()) computers.queueEvent("foo");` contains a race condition, as
   * there's no guarantee that any computers are still attached within the body of the if statement.
   *
   * @return Whether this collection is non-empty.
   */
  fun hasComputers(): Boolean {
    lock.readLock().lock()
    try {
      return !computers.isEmpty()
    } finally {
      lock.readLock().unlock()
    }
  }
}
