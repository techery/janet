package io.techery.janet;

import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.SyncedResponse;
import io.techery.janet.compiler.utils.validation.ClassValidator;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.validation.AsyncActionValidators;

@AutoService(Processor.class)
public class JanetAsyncProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Messager messager;
    private ClassValidator classValidator;
    private AsyncActionValidators asyncActionValidators;
    private AsyncWrappersGenerator wrappersGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        classValidator = new ClassValidator(AsyncAction.class);
        asyncActionValidators = new AsyncActionValidators();
        wrappersGenerator = new AsyncWrappersGenerator(processingEnv.getFiler());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new HashSet<String>();
        annotataions.add(AsyncAction.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return true;
        ArrayList<AsyncActionClass> actionClasses = new ArrayList<AsyncActionClass>();
        for (Element element : roundEnv.getElementsAnnotatedWith(AsyncAction.class)) {
            Set<ValidationError> errors = new HashSet<ValidationError>();
            errors.addAll(classValidator.validate(element));
            if (!errors.isEmpty()) {
                printErrors(errors);
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            boolean isSystem = false;
            for (TypeMirror iface : typeElement.getInterfaces()) {
                if (iface.toString().equals("io.techery.janet.async.actions.SystemAction")) {
                    isSystem = true;
                    break;
                }
            }
            if (isSystem) continue;
            AsyncActionClass actionClass = new AsyncActionClass(elementUtils, typeElement);
            errors.addAll(asyncActionValidators.validate(actionClass));
            if (!errors.isEmpty()) {
                printErrors(errors);
                continue;
            }
            actionClasses.add(actionClass);
        }
        if (!actionClasses.isEmpty()) {
            wrappersGenerator.generate(actionClasses);

        }
        return true;
    }

    private void printErrors(Collection<ValidationError> errors) {
        for (ValidationError error : errors) {
            messager.printMessage(Diagnostic.Kind.ERROR, error.getMessage(), error.getElement());
        }
    }

}