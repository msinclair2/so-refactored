package com.so_refactored.java;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.nio.file.Files;

import static junit.framework.TestCase.assertNotNull;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

public class OnlyInSOFinder {
    public static void main(String[] args) {
        // Create file path to the MSO file.
        File mso_file = new File("files/master.owl");

        // Create the OntologyManager and DataFactory for basically doing anything in the OWLAPI.
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        // Load the MSO from file.
        OWLOntology mso = null;
        try {
            mso = m.loadOntologyFromOntologyDocument(mso_file);
        } catch (OWLException e) {
            e.printStackTrace();
        }
        assertNotNull(mso);

        // Retrieve the class objects for SO and MSO.
        Set<OWLClass> mso_classes = mso.getClassesInSignature();

        // Create the IRI for the only in so property.
        IRI is_represented_only_in_SO = IRI.create("http://purl.obolibrary.org/obo/MSO_3100074");

        // Create a list to act as output file buffer.
        List<String> output = new ArrayList<>();

        // Loop through all classes in signature and test one by one for only in so annotation.
        for (OWLClass cls : mso_classes) {
            // Use entity searcher to get annotation properties.
            for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, mso)) {
                OWLAnnotationProperty ann_prop = ann.getProperty();
                IRI ann_prop_IRI = ann_prop.getIRI();
                // Scratch if this is the only in so property.
                if (ann_prop_IRI.equals(is_represented_only_in_SO)) {
                    // Get the label on this class.
                    for (OWLAnnotation label : EntitySearcher.getAnnotations(cls, mso, df.getRDFSLabel())) {
                        if (label.getValue() instanceof OWLLiteral) {
                            OWLLiteral so_label = (OWLLiteral) label.getValue();
                            System.out.println("Label: " + so_label.getLiteral());
                            output.add("Label: " + so_label.getLiteral());
                        }
                    }
                    String[] parts = cls.getIRI().toString().split("/obo/");
                    System.out.println("ID: " + parts[1] + "\n");
                    output.add("ID: " + parts[1] + "\n");
                }
            }
        }
        // Write the output buffer to file.
        try {
            Files.write(Paths.get("files/output.txt"), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
