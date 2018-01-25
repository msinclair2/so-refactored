package com.so_refactored.java;

import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OnlyInSOExtractor {
    public static void extractOnlyInSO(OWLOntology mso, OWLOntology so) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
        // Create data factory.
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        // Retrieve the class objects for SO and MSO.
        Set<OWLClass> mso_classes = mso.getClassesInSignature();

        // Create the IRI for the only in so property.
        IRI is_represented_only_in_SO = IRI.create("http://purl.obolibrary.org/obo/MSO_3100074");

        // Create a set to contain IRIs to extract.
        Set<OWLClass> onlyInSOTerms = new HashSet<>();

        // Loop through all classes in signature and test one by one for only in so annotation.
        for (OWLClass cls : mso_classes) {
            // Use entity searcher to get annotation properties.
            for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, mso)) {
                OWLAnnotationProperty ann_prop = ann.getProperty();
                IRI ann_prop_IRI = ann_prop.getIRI();
                // Test if this is the only in so property.
                if (ann_prop_IRI.equals(is_represented_only_in_SO)) {
                    onlyInSOTerms.add(cls);
                }
            }
        }
        for (OWLClass cls : onlyInSOTerms) {
            Collection<OWLAnnotationAssertionAxiom> anns = EntitySearcher.getAnnotationAssertionAxioms(cls, mso);
            OWLDeclarationAxiom declaration = df.getOWLDeclarationAxiom(cls);
            AddAxiom addAxiom = new AddAxiom(so, declaration);
            so.getOWLOntologyManager().applyChange(addAxiom);
            for (OWLAnnotationAssertionAxiom ass : anns) {
                AddAxiom newAxiom = new AddAxiom(so, ass);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
            Collection<OWLClassExpression> equivs = EntitySearcher.getEquivalentClasses(cls, mso);
            Set<OWLClassExpression> equivalents = new HashSet<>(equivs);
            if (equivalents.size() != 0) {
                OWLEquivalentClassesAxiom equivAxiom = df.getOWLEquivalentClassesAxiom(equivalents);
                AddAxiom newAxiom = new AddAxiom(so, equivAxiom);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
            Collection<OWLClassExpression> superclasses = EntitySearcher.getSuperClasses(cls, mso);
            for (OWLClassExpression superclass : superclasses) {
                OWLSubClassOfAxiom subclassOf = df.getOWLSubClassOfAxiom(cls, superclass);
                AddAxiom newAxiom = new AddAxiom(so, subclassOf);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
        }
    }
}
