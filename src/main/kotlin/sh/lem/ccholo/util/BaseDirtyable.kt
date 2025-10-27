package sh.lem.ccholo.util

interface BaseDirtyable : Dirtyable {
  var dirty: Boolean

  override fun pollDirty(): Boolean {
    val value = dirty
    dirty = false
    return value
  }

  override fun setDirty() {
    dirty = true
  }
}
