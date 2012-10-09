/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.tasks.diagnostics.internal.dsl;

import groovy.lang.Closure;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.internal.notations.NotationParserBuilder;
import org.gradle.api.internal.notations.api.NotationParser;
import org.gradle.api.internal.notations.api.UnsupportedNotationException;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.internal.ValidatingClosureSpec;

import java.util.Collection;

/**
 * by Szczepan Faber, created at: 10/9/12
 */
public class DependencyResultSpecNotationParser implements NotationParser<Spec<DependencyResult>> {

    private final String dslElement;

    DependencyResultSpecNotationParser(String dslElement) {
        this.dslElement = dslElement;
    }

    public Spec<DependencyResult> parseNotation(final Object notation) throws UnsupportedNotationException {
        //TODO SF consider exposing a separate notation parser for closure -> spec
        if (notation instanceof Closure) {
            final Closure closure = (Closure) notation;
            return new ValidatingClosureSpec<DependencyResult>(closure, dslElement);
        }
        if (notation instanceof CharSequence) {
            final String stringNotation = notation.toString();
            return new Spec<DependencyResult>() {
                public boolean isSatisfiedBy(DependencyResult candidate) {
                    //TODO SF we need a better matching
                    String candidateName = candidate.getRequested().getGroup() + ":" + candidate.getRequested().getName() + ":" + candidate.getRequested().getVersion();
                    return candidateName.contains(stringNotation);
                }
            };
        }
        throw new UnsupportedNotationException(notation);
    }

    public void describe(Collection<String> candidateFormats) {
        candidateFormats.add("String value, e.g. 'some-lib' or 'org.libs:some-lib'.");
        candidateFormats.add("Closure that returns boolean and takes a single DependencyResult as parameter.");
    }

    public static NotationParser<Spec<DependencyResult>> create() {
        return (NotationParser) new NotationParserBuilder<Spec>()
                .resultingType(Spec.class)
                .invalidNotationMessage("Please check the input for the DependencyInsight.dependency element.")
                .parser(new DependencyResultSpecNotationParser("DependencyInsight.dependency"))
                .toComposite();
    }
}