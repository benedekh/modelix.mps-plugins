package org.modelix.model.mpsadapters.mps;

/*Generated by MPS */

import org.modelix.model.api.ILanguageRepository;
import org.jetbrains.annotations.Nullable;
import org.modelix.model.api.IConcept;
import org.jetbrains.annotations.NotNull;
import jetbrains.mps.smodel.adapter.ids.SConceptId;
import jetbrains.mps.smodel.runtime.ConceptDescriptor;
import jetbrains.mps.smodel.language.ConceptRegistry;
import jetbrains.mps.smodel.runtime.illegal.IllegalConceptDescriptor;
import jetbrains.mps.smodel.adapter.structure.MetaAdapterFactory;
import java.util.List;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import java.util.ArrayList;
import jetbrains.mps.smodel.language.LanguageRegistry;
import java.util.function.Consumer;
import jetbrains.mps.smodel.language.LanguageRuntime;
import org.jetbrains.mps.openapi.language.SAbstractConcept;
import jetbrains.mps.internal.collections.runtime.Sequence;
import jetbrains.mps.internal.collections.runtime.ISelector;

public class MPSLanguageRepository implements ILanguageRepository {
  public static final MPSLanguageRepository INSTANCE = new MPSLanguageRepository();
  @Nullable
  @Override
  public IConcept resolveConcept(@NotNull String uid) {
    if (!(uid.startsWith("mps:"))) {
      return null;
    }
    SConceptId conceptId;
    try {
      conceptId = SConceptId.deserialize(uid.substring(4));
    } catch (Exception ex) {
      return null;
    }
    if (conceptId == null) {
      return null;
    }
    ConceptDescriptor conceptDescriptor = ConceptRegistry.getInstance().getConceptDescriptor(conceptId);
    if (conceptDescriptor instanceof IllegalConceptDescriptor) {
      return null;
    }
    return SConceptAdapter.wrap(MetaAdapterFactory.getAbstractConcept(conceptDescriptor));
  }

  @NotNull
  @Override
  public List<IConcept> getAllConcepts() {
    final List<IConcept> result = ListSequence.fromList(new ArrayList<IConcept>());
    LanguageRegistry.getInstance().withAvailableLanguages(new Consumer<LanguageRuntime>() {
      public void accept(LanguageRuntime language) {
        Iterable<SAbstractConcept> concepts = language.getIdentity().getConcepts();
        ListSequence.fromList(result).addSequence(Sequence.fromIterable(concepts).select(new ISelector<SAbstractConcept, IConcept>() {
          public IConcept select(SAbstractConcept it) {
            return SConceptAdapter.wrap(it);
          }
        }));
      }
    });
    return result;
  }

  @Override
  public int getPriority() {
    return 1000 - 1; // org.modelix.model.mpsadapters.MPSLanguageRepository has 1000
  }
}