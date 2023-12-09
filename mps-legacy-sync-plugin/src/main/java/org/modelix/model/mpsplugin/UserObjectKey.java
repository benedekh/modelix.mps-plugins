package org.modelix.model.mpsplugin;

/*Generated by MPS */


public class UserObjectKey<E> {
  private final String id;

  public UserObjectKey(String id1) {
    id = id1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    UserObjectKey that = (UserObjectKey) o;
    if ((id != null ? !(((Object) id).equals(that.id)) : that.id != null)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = 0;
    result = 31 * result + ((id != null ? id.hashCode() : 0));
    return result;
  }
}
