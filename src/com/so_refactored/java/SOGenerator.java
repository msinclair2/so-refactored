package com.so_refactored.java;

import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
//import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.OWLObjectTransformer;
import org.semanticweb.owlapi.util.OWLOntologyIRIChanger;

import java.io.IOException;
import java.util.*;

//import static java.util.Collections.singleton;

public class SOGenerator {
    public static void main(String[] args) throws IOException {

        IOHelper ioHelper = new IOHelper();

        OWLOntology master = ioHelper.loadOntology("files/master.owl");

        OWLOntologyManager manager = master.getOWLOntologyManager();

        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        IDChanger idChanger = new IDChanger();

        IRI isRepresentedOnlyInMSO = IRI.create("http://purl.obolibrary.org/obo/MSO_3100075");

        IRI idSO = IRI.create("http://purl.obolibrary.org/obo/SO_refactored.owl");

        Set<OWLClass> masterClasses = master.getClassesInSignature();

//        IRI independentContinuant = IRI.create("http://purl.obolibrary.org/obo/BFO_0000004");
//
//        IRI genericallyDependentContinuant = IRI.create("http://purl.obolibrary.org/obo/BFO_0000031");
//
//        OWLOntology dummyOntology = manager.createOntology();
//
//        for (OWLClass cls : masterClasses) {
//
//            Collection<OWLClassExpression> equivalentClasses = EntitySearcher.getEquivalentClasses(cls, master);
//
//            for (OWLClassExpression equivalentClass : equivalentClasses) {
//
//                if (equivalentClass.toString().contains("BFO_0000004")) {
//
//                    AddAxiom addAxiom = new AddAxiom(dummyOntology, dataFactory.getOWLEquivalentClassesAxiom(cls, equivalentClass));
//
//                    manager.applyChange(addAxiom);
//
//                }
//
//            }
//
//        }
//
//        OWLEntityRenamer renamer = new OWLEntityRenamer(manager, singleton(dummyOntology));
//
//        List<OWLOntologyChange> dummyChanges = renamer.changeIRI(independentContinuant, genericallyDependentContinuant);
//
//        for (OWLOntologyChange dummyChange : dummyChanges) {
//
//            OWLOntologyChange masterChange = dummyChange.getChangeData().createOntologyChange(master);
//
//            System.out.println(masterChange.toString());
//
//            manager.applyChange(masterChange);
//
//        }

        for (OWLClass cls : masterClasses) {

            for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, master)) {

                OWLAnnotationProperty annProp = ann.getProperty();

                IRI annPropIRI = annProp.getIRI();

                if (annPropIRI.equals(isRepresentedOnlyInMSO)) {

                    for (OWLAxiom MSOAxiom : EntitySearcher.getReferencingAxioms(cls, master)) {

                        RemoveAxiom removeAxiom = new RemoveAxiom(master, MSOAxiom);

                        master.getOWLOntologyManager().applyChange(removeAxiom);
                    }

                    break;
                }
            }
        }

        OWLObjectTransformer<IRI> replacer = new OWLObjectTransformer<>((x) -> true,
                (input) -> {

                    if (input != null) {

                        String newIRI = idChanger.changeIRIs(input.toString());

                        if (newIRI != null) {

                            return IRI.create(newIRI);
                        }
                    }

                    return input;
                },

                dataFactory,

                IRI.class);

        List<OWLOntologyChange> changes = replacer.change(master);

        manager.applyChanges(changes);

        OWLOntologyIRIChanger IRIchanger = new OWLOntologyIRIChanger(master.getOWLOntologyManager());

        List<OWLOntologyChange> IRIchanges = IRIchanger.getChanges(master, idSO);

        manager.applyChanges(IRIchanges);

        ioHelper.saveOntology(master, "files/SO.owl");
    }

}

