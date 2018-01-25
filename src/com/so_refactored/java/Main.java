package com.so_refactored.java;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;

import org.junit.Assert.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.PriorityCollection;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.nio.file.Files;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.semanticweb.owlapi.search.Searcher.annotationObjects;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import uk.ac.manchester.cs.jfact.JFactFactory;

public class Main {
    public static void main(String[] args) {
        // Load ontology.
        File file = new File("files/master.owl");

//        File target = new File("files/test.owl");

        List<String> labels = new ArrayList<String>();

        OWLOntologyManager m = OWLManager.createOWLOntologyManager();

        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLOntology o = null;
        try {
            o = m.loadOntologyFromOntologyDocument(file);
        } catch (OWLException e) {
            e.printStackTrace();
        }
        assertNotNull(o);

        // Create JFact reasoner.
        OWLReasonerFactory reasonerFactory = new JFactFactory();
        OWLReasonerConfiguration config = new SimpleConfiguration(50000);
        OWLReasoner reasoner = reasonerFactory.createReasoner(o, config);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        assertTrue(reasoner.isConsistent());

//        IRI iri = m.getOntologyDocumentIRI(o);
//
//        System.out.println(iri);

        OWLAnnotationProperty idProperty = df.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/" +
                "oboInOwl#id"));

        for (OWLClass cls : o.getClassesInSignature()) {

            for (OWLAnnotation annotation : EntitySearcher.getAnnotations(cls, o, idProperty)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    String literalString = val.getLiteral();

                    if (literalString.startsWith("SO:")) {
                        for (OWLAnnotation a : EntitySearcher.getAnnotations(cls, o, df.getRDFSLabel())) {
                            if (a.getValue() instanceof OWLLiteral) {
                                OWLLiteral label = (OWLLiteral) a.getValue();
                                System.out.println(label.getLiteral());
                                labels.add(label.getLiteral());
                            }
                        }
                        System.out.println(literalString);

                        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(cls, true);

                        for (Node<OWLClass> subNode : subClasses.getNodes()) {
                            for (OWLClass subclass : subNode.getEntities()) {
                                for (OWLAnnotation ann : EntitySearcher.getAnnotations(subclass, o, df.getRDFSLabel())) {
                                    if (ann.getValue() instanceof OWLLiteral) {
                                        OWLLiteral label = (OWLLiteral) ann.getValue();
                                        System.out.println("subclass: " + label.getLiteral());
                                        labels.add("subclass: " + label.getLiteral());
                                    }
                                }
                            }
                        }
                    }
                }
            }


//        for (OWLClass cls : o.getClassesInSignature())
//        {
//            for (OWLAnnotation annotation : annotationObjects(o.getAnnotationAssertionAxioms(cls.getIRI()),
//                    df.getRDFSLabel()))
//            {
//                if (annotation.getValue() instanceof OWLLiteral)
//                {
//                    OWLLiteral val = (OWLLiteral) annotation.getValue();
//                    System.out.println(val.getLiteral());
//                }
//            }
//        }

            // Save ontology to file.
//        try {
//            m.saveOntology(o, IRI.create(target.toURI()));
//        }
//        catch (OWLException e)
//        {
//            e.printStackTrace();
//        }


        }

        try {
            Files.write(Paths.get("files/output.txt"), labels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
