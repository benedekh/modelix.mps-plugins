package org.modelix.model.mpsplugin;

/*Generated by MPS */


public interface IUserObjectContainer {
  <T> void putUserObject(UserObjectKey<T> key, T value);
  <T> T getUserObject(UserObjectKey<T> key);
}