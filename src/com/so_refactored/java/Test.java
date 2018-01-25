package com.so_refactored.java;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectTransformer;

public class Test {
    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLOntology o = OWLManager.createOWLOntologyManager().createOntology();
        AddAxiom add = new AddAxiom(o, df.getOWLEquivalentClassesAxiom(df.getOWLClass(IRI.create("urn:test:c1")),
                df.getOWLClass(IRI.create("urn:test:c2"))));
        o.getOWLOntologyManager().applyChange(add);
        OWLObjectTransformer<OWLEntity> replacer =
                new OWLObjectTransformer<>(x -> x instanceof OWLEquivalentClassesAxiom,
                        input -> df.getOWLEntity(input.getEntityType(),

                                IRI.create(input.getIRI().toString().replace("urn:test:",
                                        "urn:test2:"))),
                        df, OWLEntity.class);
        replacer.change(o).forEach(System.out::println);
    }
}
