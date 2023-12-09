package org.modelix.model.mpsplugin;

/*Generated by MPS */

import org.modelix.model.area.IArea;
import org.modelix.model.api.PNodeAdapter;
import org.modelix.model.mpsadapters.mps.NodeToSNodeAdapter;
import org.modelix.model.mpsadapters.mps.SNodeToNodeAdapter;
import org.modelix.model.api.INodeWrapper;
import org.modelix.model.mpsadapters.mps.ANode;
import jetbrains.mps.smodel.SNode;
import org.jetbrains.mps.openapi.module.SRepository;
import org.modelix.model.mpsadapters.mps.MPSArea;
import jetbrains.mps.internal.collections.runtime.Sequence;
import jetbrains.mps.internal.collections.runtime.ISelector;
import jetbrains.mps.internal.collections.runtime.NotNullWhereFilter;
import kotlin.jvm.functions.Function0;
import kotlin.Unit;
import org.jetbrains.mps.openapi.model.SModel;

public class TransactionUtil {

  public static IArea extractArea(Object obj) {
    if (obj instanceof PNodeAdapter) {
      return ((PNodeAdapter) obj).getArea();
    } else if (obj instanceof NodeToSNodeAdapter) {
      return extractArea(SNodeToNodeAdapter.wrap(((NodeToSNodeAdapter) obj)));
    } else if (obj instanceof SNodeToNodeAdapter) {
      return extractArea(((SNodeToNodeAdapter) obj).getWrapped());
    } else if (obj instanceof IArea) {
      return (IArea) obj;
    } else if (obj instanceof INodeWrapper) {
      return extractArea(((INodeWrapper) obj).getWrappedNode());
    } else if (obj instanceof ANode) {
      return extractArea(ANode.unwrap(((ANode) obj)));
    } else if (obj instanceof SNode) {
      SRepository repository = check_276zg0_a0a0f0a1(((SNode) obj).getModel());
      return (repository == null ? new MPSArea() : new MPSArea(repository));
    } else {
      return null;
    }
  }

  public static void runWriteOnNodes(Iterable<Object> nodesToRead, Runnable r) {
    runWriteOnAreas(Sequence.fromIterable(nodesToRead).select(new ISelector<Object, IArea>() {
      public IArea select(Object it) {
        return extractArea(it);
      }
    }).where(new NotNullWhereFilter<IArea>()), r);
  }

  public static void runWriteOnNode(Object nodeToRead, Runnable r) {
    runWriteOnNodes(Sequence.<Object>singleton(nodeToRead), r);
  }

  public static void runWriteOnAreas(final Iterable<IArea> areasToRead, final Runnable r) {
    if (Sequence.fromIterable(areasToRead).isEmpty()) {
      r.run();
    } else {
      Sequence.fromIterable(areasToRead).first().executeWrite(new Function0<Unit>() {
        public Unit invoke() {
          runReadOnAreas(Sequence.fromIterable(areasToRead).skip(1), r);
          return Unit.INSTANCE;
        }
      });
    }
  }

  public static void runReadOnNode(Object nodesToRead, Runnable r) {
    runReadOnNodes(Sequence.<Object>singleton(nodesToRead), r);
  }

  public static void runReadOnNodes(Iterable<Object> nodesToRead, Runnable r) {
    runReadOnAreas(Sequence.fromIterable(nodesToRead).select(new ISelector<Object, IArea>() {
      public IArea select(Object it) {
        return extractArea(it);
      }
    }).where(new NotNullWhereFilter<IArea>()), r);
  }

  public static void runReadOnAreas(final Iterable<IArea> areasToRead, final Runnable r) {
    if (Sequence.fromIterable(areasToRead).isEmpty()) {
      r.run();
    } else {
      Sequence.fromIterable(areasToRead).first().executeRead(new Function0<Unit>() {
        public Unit invoke() {
          runReadOnAreas(Sequence.fromIterable(areasToRead).skip(1), r);
          return Unit.INSTANCE;
        }
      });
    }
  }
  private static SRepository check_276zg0_a0a0f0a1(SModel checkedDotOperand) {
    if (null != checkedDotOperand) {
      return checkedDotOperand.getRepository();
    }
    return null;
  }
}
