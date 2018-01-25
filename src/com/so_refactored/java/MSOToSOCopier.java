package com.so_refactored.java;

import org.obolibrary.obo2owl.OwlStringTools;
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MSOToSOCopier {
    public static void main(String[] args) throws IOException, OWLOntologyCreationException, OwlStringTools.OwlStringException {
        IOHelper ioHelper = new IOHelper();

        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLOntology mso = ioHelper.loadOntology("files/master.owl");
        OWLOntology so = OWLManager.createOWLOntologyManager().createOntology();

        IDChanger id_changer = new IDChanger();

        // Retrieve all annotation properties.
        Set<OWLAnnotationProperty> annotationProperties = mso.getAnnotationPropertiesInSignature();

        // Retrieve all object properties.
        Set<OWLObjectProperty> objectProperties = mso.getObjectPropertiesInSignature();

        // Retrieve all data properties.
        Set<OWLDataProperty> dataProperties = mso.getDataPropertiesInSignature();

        // Retrieve the class objects for SO and MSO.
        Set<OWLClass> mso_classes = mso.getClassesInSignature();

        // Create the IRI for the only in so property.
        IRI is_represented_only_in_SO = IRI.create("http://purl.obolibrary.org/obo/MSO_3100074");

        // Create the IRI for the only in mso property.
        IRI is_represented_only_in_MSO = IRI.create("http://purl.obolibrary.org/obo/MSO_3100075");

        // Create a set to contain IRIs to copy.
        Set<OWLClass> TermsToCopyMSOToSO = new HashSet<>();

        // Create a set that contains IRIs for those terms only in SO.
        Set<OWLClass> onlyInSO = new HashSet<>();

        // First declare and copy annotations for all annotation, object, and data properties.
        // Annotation properties.
        for (OWLAnnotationProperty annotationProperty : annotationProperties) {
            // Get annotations on the property.
            Collection<OWLAnnotationAssertionAxiom> anns = EntitySearcher.getAnnotationAssertionAxioms(annotationProperty, mso);
            // Change the annotation property ID.
            OWLAnnotationProperty annotationPropertySO = id_changer.changePropertyID(annotationProperty, df);
            OWLDeclarationAxiom declaration = df.getOWLDeclarationAxiom(annotationPropertySO);
            AddAxiom addAxiom = new AddAxiom(so, declaration);
            so.getOWLOntologyManager().applyChange(addAxiom);
            for (OWLAnnotationAssertionAxiom ann : anns) {
                OWLAnnotationAssertionAxiom axiomChanged = id_changer.ChangeAnnotation(ann, df);
                AddAxiom newAxiom = new AddAxiom(so, axiomChanged);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
        }

        // Object properties.
        for (OWLObjectProperty objectProperty : objectProperties) {
            // Get annotations on the property.
            Collection<OWLAnnotationAssertionAxiom> anns = EntitySearcher.getAnnotationAssertionAxioms(objectProperty, mso);
            OWLObjectProperty new_prop = id_changer.changePropertyID(objectProperty);
            OWLDeclarationAxiom declaration = df.getOWLDeclarationAxiom(new_prop);
            AddAxiom addAxiom = new AddAxiom(so, declaration);
            so.getOWLOntologyManager().applyChange(addAxiom);
            for (OWLAnnotationAssertionAxiom ann : anns) {
                OWLAnnotationAssertionAxiom axiomChanged = id_changer.ChangeAnnotation(ann, df);
                AddAxiom newAxiom = new AddAxiom(so, axiomChanged);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
            // Copy SubObjectPropertyOf axioms.
            Set<OWLSubObjectPropertyOfAxiom> axioms = id_changer.changeSubObjectPropertyOf(objectProperty, mso, df);
            for (OWLSubObjectPropertyOfAxiom axiom : axioms) {
                AddAxiom newAxiom = new AddAxiom(so, axiom);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
        }

        // Data properties.
        for (OWLDataProperty dataProperty : dataProperties) {
            // Get annotations on the property.
            Collection<OWLAnnotationAssertionAxiom> anns = EntitySearcher.getAnnotationAssertionAxioms(dataProperty, mso);
            OWLDataProperty new_prop = id_changer.changePropertyID(dataProperty);
            OWLDeclarationAxiom declaration = df.getOWLDeclarationAxiom(new_prop);
            AddAxiom addAxiom = new AddAxiom(so, declaration);
            so.getOWLOntologyManager().applyChange(addAxiom);
            for (OWLAnnotationAssertionAxiom ann : anns) {
                OWLAnnotationAssertionAxiom axiomChanged = id_changer.ChangeAnnotation(ann, df);
                AddAxiom newAxiom = new AddAxiom(so, axiomChanged);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
        }

        // Loop through all classes in signature and test one by one for only in so annotation.
        for (OWLClass cls : mso_classes) {
            // Set a boolean flag for only in so or mso properties.
            boolean only_in = false;
            // Use entity searcher to get annotation properties.
            for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, mso)) {
                OWLAnnotationProperty ann_prop = ann.getProperty();
                IRI ann_prop_IRI = ann_prop.getIRI();
                // Test if this is the only in so or mso property.
                if (ann_prop_IRI.equals(is_represented_only_in_SO)) {
                    only_in = true;
                    onlyInSO.add(cls);
                    break;
                }
                if (ann_prop_IRI.equals(is_represented_only_in_MSO)) {
                    only_in = true;
                    break;
                }
            }
            if (!only_in) {
                TermsToCopyMSOToSO.add(cls);
            }
        }
        for (OWLClass cls : TermsToCopyMSOToSO) {
            Collection<OWLAnnotationAssertionAxiom> anns = EntitySearcher.getAnnotationAssertionAxioms(cls, mso);
            // Create an empty class using the 7-digit ID of cls but with SO not MSO as the prefix.
            OWLClass new_cls = id_changer.changeClassID(cls);
            OWLDeclarationAxiom declaration = df.getOWLDeclarationAxiom(new_cls);
            AddAxiom addAxiom = new AddAxiom(so, declaration);
            so.getOWLOntologyManager().applyChange(addAxiom);
            // Copy Annotation axioms.
            for (OWLAnnotationAssertionAxiom ann : anns) {
                OWLAnnotationAssertionAxiom axiomChanged = id_changer.ChangeAnnotation(ann, df);
                AddAxiom newAxiom = new AddAxiom(so, axiomChanged);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
            // Copy SubClassOf axioms.
            Set<OWLAxiom> axioms = id_changer.changeSubClassOf(cls, mso, df);
            for (OWLAxiom axiom : axioms) {
                    AddAxiom newAxiom = new AddAxiom(so, axiom);
                    so.getOWLOntologyManager().applyChange(newAxiom);
                }

        }
        for (OWLClass cls : onlyInSO) {
            Collection<OWLAnnotationAssertionAxiom> anns = EntitySearcher.getAnnotationAssertionAxioms(cls, mso);
            // Create an empty class using the 7-digit ID of cls but with SO not MSO as the prefix.
            OWLClass new_cls = id_changer.changeClassID(cls);
            OWLDeclarationAxiom declaration = df.getOWLDeclarationAxiom(new_cls);
            AddAxiom addAxiom = new AddAxiom(so, declaration);
            so.getOWLOntologyManager().applyChange(addAxiom);
            // Copy Annotation axioms.
            for (OWLAnnotationAssertionAxiom ann : anns) {
                OWLAnnotationAssertionAxiom axiomChanged = id_changer.ChangeAnnotation(ann, df);
                AddAxiom newAxiom = new AddAxiom(so, axiomChanged);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
            // Copy SubClassOf axioms.
            Set<OWLAxiom> axioms = id_changer.changeSubClassOf(cls, mso, df);
            for (OWLAxiom axiom : axioms) {
                AddAxiom newAxiom = new AddAxiom(so, axiom);
                so.getOWLOntologyManager().applyChange(newAxiom);
            }
        }
        ioHelper.saveOntology(so, "files/SO.owl");
    }
}
