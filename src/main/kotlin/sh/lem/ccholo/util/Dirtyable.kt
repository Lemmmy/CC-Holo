package sh.lem.ccholo.util

interface Dirtyable {
  fun pollDirty(): Boolean
  fun setDirty()
}
