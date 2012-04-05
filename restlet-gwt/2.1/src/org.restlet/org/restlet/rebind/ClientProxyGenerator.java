/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.rebind;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

import org.restlet.client.Client;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.Form;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.data.Preference;
import org.restlet.client.data.Protocol;
import org.restlet.client.engine.resource.GwtClientProxy;
import org.restlet.client.representation.ObjectRepresentation;
import org.restlet.client.representation.Representation;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Delete;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Post;
import org.restlet.client.resource.Put;
import org.restlet.client.resource.ResourceException;
import org.restlet.client.resource.Result;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.rpc.RestletBlackListTypeFilter;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracle;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracleBuilder;
import com.google.gwt.user.rebind.rpc.TypeSerializerCreator;

/**
 * Deferred binding generator capable of creating a subclass of
 * {@link ClientProxy} implementing a custom Java interface, extending the
 * marker {@link ClientProxy} interface, and annotated with Restlet annotations
 * such as {@link Get}, {@link Post}, {@link Put} or {@link Delete}.
 * 
 * @author Jerome Louvel
 */
public class ClientProxyGenerator extends com.google.gwt.core.ext.Generator {

    /** The name of the proxy class that will be generated. */
    private String className;

    /** The parent type. */
    private JClassType classType;

    /** Provides metadata to defered binding generators. */
    private GeneratorContext context;

    /** The logger used by the deferred binding generators. */
    private TreeLogger logger;

    /** The package name of the parent type. */
    private String packageName;

    /** Used to write the content of the proxy class. */
    private SourceWriter sourceWriter;

    /** Represents the serializer class. */
    private SerializableTypeOracle sto;

    /** The builder of the serializer class. */
    private SerializableTypeOracleBuilder stob;

    /** The type oracle for the current generator context. */
    private TypeOracle typeOracle;

    /**
     * The parent type for which a default constructible subclass will be
     * generated.
     */
    private String typeQName;

    /**
     * Default constructor.
     */
    public ClientProxyGenerator() {
        this.logger = null;
        this.classType = null;
        this.context = null;
        this.className = null;
        this.packageName = null;
        this.typeQName = null;
        this.sourceWriter = null;
        this.typeOracle = null;
        this.stob = null;
        this.sto = null;
    }

    /**
     * Add a new type to be serialized.
     * 
     * @param type
     *            The type to add.
     */
    private void addRootType(JType type) {
        try {
            if (!(type.isClass() != null && (getTypeOracle().getType(
                    Representation.class.getName()).isAssignableFrom(
                    type.isClass()) || getTypeOracle().getType(
                    Form.class.getName()).isAssignableFrom(type.isClass())))) {
                // Do not render Representation and Form instances
                getStob().addRootType(getLogger(), type);
            }
        } catch (Exception e) {
            getStob().addRootType(getLogger(), type);
        }
    }

    /**
     * Generates the name of the given parameter's type.
     * 
     * @param parameterType
     *            the Java parameter's type.
     * @param genericParameterType
     *            I'ts
     * @param parameterizedType
     * @return
     */
    private String buildParameterTypeName(Class<?> parameterType,
            java.lang.reflect.Type genericParameterType,
            ParameterizedType parameterizedType) {
        java.lang.reflect.Type type;
        if (parameterizedType != null) {
            type = parameterizedType.getActualTypeArguments()[0];
        } else {
            type = null;
        }
        if (type == null) {
            return parameterType.getName();
        } else {
            StringBuilder sb = new StringBuilder();
            buildTypeName(genericParameterType, sb);
            return sb.toString();
        }
    }

    /**
     * Generates the name of the given type into the given StringBuilder.
     * 
     * @param type
     *            The type.
     * @param sb
     *            The stringBuilder to complete.
     */
    private void buildTypeName(java.lang.reflect.Type type, StringBuilder sb) {
        if (type instanceof Class<?>) {
            sb.append(((Class<?>) type).getName());
        } else if (type instanceof GenericArrayType) {
            buildTypeName(((GenericArrayType) type).getGenericComponentType(),
                    sb);
            sb.append("[]");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            buildTypeName(t.getRawType(), sb);
            sb.append("<");
            if (t.getActualTypeArguments().length >= 1) {
                buildTypeName(t.getActualTypeArguments()[0], sb);
                for (int i = 1; i < t.getActualTypeArguments().length; i++) {
                    sb.append(", ");
                    buildTypeName(t.getActualTypeArguments()[i], sb);
                }
            }

            sb.append(">");
        } else {
            sb.append(type.toString());
        }
    }

    @Override
    public String generate(TreeLogger logger, GeneratorContext context,
            String typeQName) throws UnableToCompleteException {
        String result = null;

        try {
            this.logger = logger;
            this.context = context;
            this.typeQName = typeQName;
            this.sourceWriter = null;
            this.typeOracle = context.getTypeOracle();
            this.classType = typeOracle.getType(typeQName);
            this.packageName = getClassType().getPackage().getName();
            this.className = getClassType().getSimpleSourceName() + "Impl";
            generateProxy();
            result = getPackageName() + "." + getClassName();
        } catch (Exception e) {
            e.printStackTrace();
            UnableToCompleteException utce = new UnableToCompleteException();
            utce.initCause(e);
            throw utce;
        }

        return result;
    }

    /**
     * Generates the default constructor.
     */
    protected void generateContructor() {
        println("public " + getClassName() + "() {");
        indent();
        println("super(GWT.getModuleBaseURL(),");
        indent();
        println("SERIALIZATION_POLICY, ");
        println("SERIALIZER);");
        outdent();
        outdent();
        println("}");
    }

    /**
     * Generates the private members of the proxy class.
     * 
     * @param serializableTypeOracle
     *            The type of the object serializer/deserializer.
     */
    protected void generateFields(SerializableTypeOracle serializableTypeOracle) {
        String tsn = getTypeSerializerQualifiedName(getClassType());

        println("private static final String SERIALIZATION_POLICY =\"null\";");
        println("private static final " + tsn + " SERIALIZER = new " + tsn
                + "();");
    }

    /**
     * Generates the code of a given Java method (must be correctly annotated
     * using Restlet annotation).
     * 
     * @param method
     *            The Java method.
     * @throws Exception
     */
    protected void generateMethod(java.lang.reflect.Method method)
            throws Exception {
        AnnotationInfo info = AnnotationUtils.getAnnotation(method);

        if (info == null) {
            if (method.getAnnotations() != null
                    && method.getAnnotations().length > 0) {
                // Try to detect anomalies in the annotations definition
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().getName()
                            .startsWith("org." + "restlet.resource")) {
                        throw new IllegalArgumentException(
                                "The "
                                        + getTypeQName()
                                        + " interface contains a \""
                                        + method.getName()
                                        + "\" method that refers to an annotation that does not come from the Restlet edition for GWT: "
                                        + annotation.annotationType().getName()
                                        + ". The correct package name must be \"org.restlet.client.resource\".");
                    }
                }
            }
            getLogger()
                    .log(Type.WARN,
                            "The "
                                    + getTypeQName()
                                    + " interface contains a \""
                                    + method.getName()
                                    + "\" method without a proper annotation taken from the Restlet edition for GWT.");
        } else {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                print("public ");
            } else if (Modifier.isPrivate(modifiers)) {
                print("private ");
            } else if (Modifier.isProtected(modifiers)) {
                print("protected ");
            }

            print("void " + method.getName() + "(");

            Class<?>[] parameterTypes = method.getParameterTypes();
            java.lang.reflect.Type[] genericParameterTypes = method
                    .getGenericParameterTypes();
            Class<?> parameterType;
            java.lang.reflect.Type genericParameterType;
            ParameterizedType parameterizedType;

            // The return type of the callback method.
            java.lang.reflect.Type callbackType = null;
            String firstParameterTypeName = null;
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterType = parameterTypes[i];
                genericParameterType = genericParameterTypes[i];
                parameterizedType = (genericParameterType instanceof ParameterizedType) ? (ParameterizedType) genericParameterType
                        : null;

                if (i == (parameterTypes.length - 1)) {
                    // By convention, only the last parameter can describe the
                    // return clause of the callback method.
                    if (AsyncCallback.class.isAssignableFrom(parameterType)
                            || Result.class.isAssignableFrom(parameterType)) {
                        if (parameterizedType != null) {
                            callbackType = parameterizedType
                                    .getActualTypeArguments()[0];
                        } else {
                            callbackType = null;
                        }
                        print("final ");
                        print(buildParameterTypeName(parameterType,
                                genericParameterType, parameterizedType));
                        print(" callback");
                    } else {
                        getLogger()
                                .log(Type.WARN,
                                        "The "
                                                + getTypeQName()
                                                + " interface contains a \""
                                                + method.getName()
                                                + "\" method without a callback as last parameter.");
                    }
                } else {
                    // By convention, only the first parameter can be passed as
                    // single parameter of the callback method.
                    if (i == 0) {
                        firstParameterTypeName = buildParameterTypeName(
                                parameterType, genericParameterType,
                                parameterizedType);
                        print(firstParameterTypeName);
                    } else {
                        print(buildParameterTypeName(parameterType,
                                genericParameterType, parameterizedType));
                    }

                    print(" param" + (i + 1) + ", ");
                }
            }

            println("){");
            indent();
            if (parameterTypes.length > 1) {
                // By convention, only the first parameter can be passed as
                // single parameter of the callback method.
                parameterType = parameterTypes[0];

                if (Representation.class.isAssignableFrom(parameterType)) {
                    println("getClientResource().getRequest().setEntity(param1);");
                } else if (Form.class.isAssignableFrom(parameterType)) {
                    println("getClientResource().getRequest().setEntity((param1 == null) ? null : param1.getWebRepresentation());");
                } else {
                    println("Representation requestEntity = new ObjectRepresentation<"
                            + firstParameterTypeName
                            + ">((SerializationStreamFactory) "
                            + getClassName() + ".this, param1);");
                    println("getClientResource().getRequest().setEntity(requestEntity);");
                }
            } else {
                println("getClientResource().getRequest().setEntity(null);");
            }
            println("getClientResource().getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT_GWT));");
            println("getClientResource().setMethod(Method."
                    + info.getRestletMethod().toString() + ");");
            println("getClientResource().setOnResponse(new Uniform() {");
            indent();
            println("public void handle(Request request, Response response) {");
            indent();
            println("if (getClientResource().getStatus().isError()) {");
            indent();
            println("callback.onFailure(new ResourceException(getClientResource().getStatus()));");
            outdent();
            println("} else {");
            indent();

            if (Void.class.equals(callbackType) || callbackType == null) {
                println("callback.onSuccess(null);");
            } else {
                StringBuilder sb = new StringBuilder();
                buildTypeName(callbackType, sb);
                println(sb.toString() + " result = null;");
                println("boolean serializationError = false;");
                println();
                println("try {");
                indent();
                println("if(response.isEntityAvailable()){");
                indent();
                println("if (MediaType.APPLICATION_JAVA_OBJECT_GWT.equals(response.getEntity().getMediaType())) {");
                indent();
                println("result = new ObjectRepresentation<" + sb.toString()
                        + ">(");
                indent();
                println("response.getEntity().getText(),");
                println("(SerializationStreamFactory) " + getClassName()
                        + ".this, "
                        + String.class.getName().equals(sb.toString()) + ")");
                println(".getObject();");
                outdent();
                outdent();
                println("} else {");
                indent();
                if (String.class.getName().equals(sb.toString())) {
                    println("result = response.getEntity().getText();");
                } else {
                    println("throw new IOException(\"Can't parse the enclosed entity because of its media type. Expected <\" + MediaType.APPLICATION_JAVA_OBJECT_GWT + \"> but was <\" + response.getEntity().getMediaType() + \">. Make sure you have added the org.restlet.client.ext.gwt.jar file to your server.\");");
                }
                outdent();
                println("}");
                outdent();
                println("}");
                outdent();
                println("} catch (Throwable e) {");
                indent();
                println("// Serialization error, considered as a communication error.");
                println("serializationError = true;");
                println("callback.onFailure(new ResourceException(e));");
                outdent();
                println("}");
                println();
                println("if (!serializationError) {");
                indent();
                println("callback.onSuccess(result);");
                outdent();
                println("}");
            }
            outdent();
            println("}");
            outdent();
            println("}");
            outdent();
            println("});");
            println("getClientResource().handle();");
            outdent();
            println("}");
        }
    }

    /**
     * Generates the code of the proxy class.
     * 
     * @throws Exception
     */
    protected void generateProxy() throws Exception {
        PrintWriter printWriter = getContext().tryCreate(getLogger(),
                getPackageName(), getClassName());

        if (printWriter != null) {
            generateSerializers();

            ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
                    getPackageName(), getClassName());
            composer.setSuperclass(GwtClientProxy.class.getCanonicalName());
            composer.addImplementedInterface(getTypeQName());
            composer.addImport(GWT.class.getCanonicalName());
            composer.addImport(IOException.class.getCanonicalName());
            composer.addImport(Client.class.getCanonicalName());
            composer.addImport(Protocol.class.getCanonicalName());
            composer.addImport(Preference.class.getCanonicalName());
            composer.addImport(Request.class.getCanonicalName());
            composer.addImport(Response.class.getCanonicalName());
            composer.addImport(Uniform.class.getCanonicalName());
            composer.addImport(Method.class.getCanonicalName());
            composer.addImport(Representation.class.getCanonicalName());
            composer.addImport(ObjectRepresentation.class.getCanonicalName());
            composer.addImport(ResourceException.class.getCanonicalName());
            composer.addImport(AsyncCallback.class.getCanonicalName());
            composer.addImport(Result.class.getCanonicalName());
            composer.addImport(SerializationStreamFactory.class
                    .getCanonicalName());
            composer.addImport(MediaType.class.getCanonicalName());

            this.sourceWriter = composer.createSourceWriter(getContext(),
                    printWriter);
            println();
            generateFields(getSto());
            println();
            generateContructor();
            println();

            // Generate the proxy methods
            Class<?> interfaceClass = Class.forName(getTypeQName());

            for (java.lang.reflect.Method method : interfaceClass.getMethods()) {
                if (!"getClientResource".equals(method.getName())) {
                    if ("void".equals(method.getReturnType().getName())) {
                        generateMethod(method);
                        println();
                    } else {
                        getLogger()
                                .log(Type.WARN,
                                        "The "
                                                + getTypeQName()
                                                + " interface contains a \""
                                                + method.getName()
                                                + "\" method without a void return type.");
                    }
                }
            }

            outdent();
            println("}");
            getContext().commit(getLogger(), printWriter);
        }
    }

    /**
     * Generates the code of the necessary object serializers.
     * 
     * @throws UnableToCompleteException
     */
    protected void generateSerializers() throws UnableToCompleteException {
        try {
            try {
                this.stob = getSerializableTypeOracleBuilder2_3();
            } catch (Exception e2_3) {
                try {
                    this.stob = getSerializableTypeOracleBuilder2_2();
                } catch (Exception e2_2) {
                    getLogger().log(TreeLogger.ERROR, "", e2_2);
                    throw new UnableToCompleteException();
                }
            }

            this.stob.setTypeFilter(new RestletBlackListTypeFilter(getLogger(),
                    getContext().getPropertyOracle()));

            // Discover and add serializable types
            getStob().addRootType(getLogger(),
                    getTypeOracle().getType(String.class.getName()));
            JMethod[] methods = getClassType().getOverridableMethods();
            JClassType exceptionClass = getTypeOracle().getType(
                    Exception.class.getName());
            JType asyncType = getTypeOracle().getType(
                    AsyncCallback.class.getName());
            JType cbType = getTypeOracle().getType(Result.class.getName());

            for (JMethod method : methods) {
                if (!"getClientResource".equals(method.getName())) {
                    JType returnType = method.getReturnType();

                    if (returnType != JPrimitiveType.VOID) {
                        getStob().addRootType(getLogger(), returnType);
                    }

                    JParameter[] parameters = method.getParameters();
                    for (JParameter parameter : parameters) {
                        JType parameterType = parameter.getType();
                        JParameterizedType parameterizedType = parameterType
                                .isParameterized();
                        if (parameterizedType == null) {
                            // Non generic type.
                            addRootType(parameterType);
                        } else if (((parameterizedType.getBaseType() != asyncType) && (parameterizedType
                                .getBaseType() != cbType))) {
                            // Generic type but not a callback.
                            addRootType(parameterType);
                        } else {
                            // Callback type, inspect the "inner" type.
                            if (parameterizedType.getTypeArgs().length > 0) {
                                JClassType t = parameterizedType.getTypeArgs()[0];
                                // Add the enclosed type if it is not equals to
                                // java.lang.Void.
                                if (t != null
                                        && !("Void".equals(t.getName()) && "java.lang"
                                                .equals(t.getPackage()
                                                        .getName()))) {
                                    addRootType(t);
                                }
                            }
                        }
                    }

                    JType[] exceptions = method.getThrows();
                    if (exceptions.length > 0) {
                        for (JType exception : exceptions) {
                            if (!exceptionClass.isAssignableFrom(exception
                                    .isClass())) {
                                getLogger()
                                        .log(Type.WARN,
                                                "Only checked exceptions are supported");
                            }
                            addRootType(exception);
                        }
                    }
                }
            }

            // Log serialization information
            OutputStream los = getContext().tryCreateResource(getLogger(),
                    getClassType().getQualifiedSourceName() + ".restlet.log");
            java.lang.reflect.Method setLogOs = null;
            Object setLogOsParameter = null;
            try {
                setLogOs = getStob().getClass().getDeclaredMethod(
                        "setLogOutputStream", OutputStream.class);
                setLogOsParameter = los;
            } catch (SecurityException e1) {
            } catch (NoSuchMethodException e) {
                try {
                    setLogOs = getStob().getClass().getDeclaredMethod(
                            "setLogOutputWriter", PrintWriter.class);
                    setLogOsParameter = new PrintWriter(los);
                } catch (SecurityException e1) {
                } catch (NoSuchMethodException e1) {
                }
            }
            if (setLogOs != null) {
                try {
                    setLogOs.invoke(getStob(), setLogOsParameter);
                } catch (Throwable e) {
                    getLogger().log(Type.WARN,
                            "Cannot set the log writer " + setLogOs.getName());
                }
            } else {
                getLogger().log(Type.WARN,
                        "Cannot set the log writer for the compilation phase.");
            }
            this.sto = getStob().build(getLogger());

            if (los != null) {
                getContext().commitResource(getLogger(), los).setPrivate(true);
            }

            TypeSerializerCreator tsc = null;
            try {
                tsc = getTypeSerializerCreatorGwt2_1();
            } catch (Exception e2_1) {
                try {
                    tsc = getTypeSerializerCreatorGwt2_2();
                } catch (Exception e2_2) {
                    try {
                        tsc = getTypeSerializerCreatorGwt2_0();
                    } catch (Exception e2_0) {
                        try {
                            tsc = getTypeSerializerCreatorGwt1_7();
                        } catch (Exception e1_7) {
                            getLogger().log(TreeLogger.ERROR, "", e1_7);
                            throw new UnableToCompleteException();
                        }
                    }
                }
            }
            if (tsc != null) {
                tsc.realize(getLogger());
            } else {
                getLogger().log(TreeLogger.ERROR,
                        "Cannot create a TypeSerializerCreator instance.");
                throw new UnableToCompleteException();
            }
        } catch (NotFoundException e) {
            getLogger().log(TreeLogger.ERROR, "", e);
            throw new UnableToCompleteException();
        }
    }

    /**
     * Returns the name of the proxy class that will be generated.
     * 
     * @return The name of the proxy class that will be generated.
     */
    protected String getClassName() {
        return className;
    }

    /**
     * Returns the parent type.
     * 
     * @return The parent type.
     */
    protected JClassType getClassType() {
        return classType;
    }

    /**
     * Returns the context object that provides metadata to defered binding
     * generators.
     * 
     * @return The context object that provides metadata to defered binding
     *         generators.
     */
    protected GeneratorContext getContext() {
        return context;
    }

    /**
     * Returns the logger used by the deferred binding generator.
     * 
     * @return The logger used by the deferred binding generator.
     */
    protected TreeLogger getLogger() {
        return logger;
    }

    /**
     * Returns the package name of the parent type.
     * 
     * @return The package name of the parent type.
     */
    protected String getPackageName() {
        return packageName;
    }

    /**
     * Instantiates a SerializableTypeOracleBuilder instance using the GWT2.2
     * and before constructor.
     * 
     * @return An instance of a {@link SerializableTypeOracleBuilder}.
     * @throws Exception
     */
    private SerializableTypeOracleBuilder getSerializableTypeOracleBuilder2_2()
            throws Exception {
        SerializableTypeOracleBuilder result = null;

        Constructor<SerializableTypeOracleBuilder> c = null;
        c = SerializableTypeOracleBuilder.class.getDeclaredConstructor(
                TreeLogger.class, PropertyOracle.class, TypeOracle.class);
        result = c.newInstance(getLogger(), getContext().getPropertyOracle(),
                getTypeOracle());

        return result;
    }

    /**
     * Instantiates a SerializableTypeOracleBuilder instance using the GWT2.3
     * constructor.
     * 
     * @return An instance of a {@link SerializableTypeOracleBuilder}.
     * @throws Exception
     */
    private SerializableTypeOracleBuilder getSerializableTypeOracleBuilder2_3()
            throws Exception {
        SerializableTypeOracleBuilder result = null;

        // At the date of 04 may 2011, the GWT GeneratorContextExt class
        // is considered as experimental and surely to be removed from gwt
        // package.
        Class<?> genClass = Class
                .forName("com.google.gwt.core.ext.GeneratorContextExt");

        Constructor<SerializableTypeOracleBuilder> c = null;
        c = SerializableTypeOracleBuilder.class.getDeclaredConstructor(
                TreeLogger.class, PropertyOracle.class, genClass);
        result = c.newInstance(getLogger(), getContext().getPropertyOracle(),
                getContext());

        return result;
    }

    /**
     * Returns the writer used for the content of the proxy class.
     * 
     * @return The writer used for the content of the proxy class.
     */
    protected SourceWriter getSourceWriter() {
        return sourceWriter;
    }

    /**
     * Returns the serializer class.
     * 
     * @return The serializer class.
     */
    protected SerializableTypeOracle getSto() {
        return sto;
    }

    /**
     * Returns the builder of the serializer class.
     * 
     * @return The builder of the serializer class.
     */
    protected SerializableTypeOracleBuilder getStob() {
        return stob;
    }

    /**
     * Returns the type oracle for the current generator context.
     * 
     * @return The type oracle for the current generator context.
     */
    protected TypeOracle getTypeOracle() {
        return typeOracle;
    }

    /**
     * Returns the parent type for which a default constructible subclass will
     * be generated.
     * 
     * @return The parent type for which a default constructible subclass will
     *         be generated.
     */
    protected String getTypeQName() {
        return typeQName;
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT1.7
     * constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt1_7()
            throws Exception {
        TypeSerializerCreator result = null;

        Constructor<TypeSerializerCreator> c = null;
        c = TypeSerializerCreator.class.getDeclaredConstructor(
                TreeLogger.class, SerializableTypeOracle.class,
                GeneratorContext.class, String.class);
        result = c.newInstance(getLogger(), getSto(), getContext(),
                getTypeSerializerQualifiedName(getClassType()));

        return result;
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT2.0
     * constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt2_0()
            throws Exception {
        TypeSerializerCreator result = null;

        Constructor<TypeSerializerCreator> c = null;
        c = TypeSerializerCreator.class.getDeclaredConstructor(
                TreeLogger.class, SerializableTypeOracle.class,
                SerializableTypeOracle.class, GeneratorContext.class,
                String.class);
        result = c.newInstance(getLogger(), getSto(), getSto(), getContext(),
                getTypeSerializerQualifiedName(getClassType()));

        return result;
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT2.1
     * constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt2_1()
            throws Exception {
        TypeSerializerCreator result = null;

        Constructor<TypeSerializerCreator> c = null;
        c = TypeSerializerCreator.class.getDeclaredConstructor(
                TreeLogger.class, SerializableTypeOracle.class,
                SerializableTypeOracle.class, GeneratorContext.class,
                String.class, String.class);
        result = c.newInstance(getLogger(), getSto(), getSto(), getContext(),
                getTypeSerializerQualifiedName(getClassType()),
                getTypeSerializerSimpleName(getClassType()));

        return result;
    }

    /**
     * Instantiates a TypeSerializerCreator instance using the GWT2.2
     * constructor.
     * 
     * @return An instance of a TypeSerializerCreator.
     * @throws Exception
     */
    private TypeSerializerCreator getTypeSerializerCreatorGwt2_2()
            throws Exception {
        TypeSerializerCreator result = null;
        // At the date of 14 february 2011, the GWT GeneratorContextExt class
        // is considered as experimental and surely to be removed from gwt
        // package.
        Constructor<TypeSerializerCreator> c = null;

        try {
            Class<?> genClass = Class
                    .forName("com.google.gwt.core.ext.GeneratorContextExt");
            c = TypeSerializerCreator.class.getDeclaredConstructor(
                    TreeLogger.class, SerializableTypeOracle.class,
                    SerializableTypeOracle.class, genClass, String.class,
                    String.class);
            result = c.newInstance(getLogger(), getSto(), getSto(),
                    genClass.cast(getContext()),
                    getTypeSerializerQualifiedName(getClassType()),
                    getTypeSerializerSimpleName(getClassType()));
        } catch (ClassNotFoundException e) {
            c = TypeSerializerCreator.class.getDeclaredConstructor(
                    TreeLogger.class, SerializableTypeOracle.class,
                    SerializableTypeOracle.class, GeneratorContext.class,
                    String.class, String.class);
            result = c.newInstance(getLogger(), getSto(), getSto(),
                    getContext(),
                    getTypeSerializerQualifiedName(getClassType()),
                    getTypeSerializerSimpleName(getClassType()));
        }

        return result;
    }

    /**
     * Returns the qualified name of the given type serializer.
     * 
     * @param type
     *            The type serializer's type object.
     * @return The qualified name of the given type serializer.
     */
    private String getTypeSerializerQualifiedName(JClassType type) {
        StringBuilder sb = new StringBuilder();

        JType leafType = type.getLeafType();
        JClassType classOrInterface = leafType.isClassOrInterface();
        // Add the package name.
        String packageName = classOrInterface.getPackage().getName();
        if (packageName != null && packageName.length() > 0) {
            sb.append(packageName).append(".");
        }
        // Add the class name
        String className = classOrInterface.getName();
        className = className.replace('.', '_');
        sb.append(className).append("_TypeSerializer");
        return sb.toString();
    }

    /**
     * Returns the simple name of the given type serializer.
     * 
     * @param type
     *            The type serializer's type object.
     * @return The simple name of the given type serializer.
     */
    private String getTypeSerializerSimpleName(JClassType type) {
        StringBuilder sb = new StringBuilder();
        JType leafType = type.getLeafType();
        JClassType classOrInterface = leafType.isClassOrInterface();
        // Add the class name
        String className = classOrInterface.getName();
        className = className.replace('.', '_');
        sb.append(className).append("_TypeSerializer");
        return sb.toString();
    }

    /**
     * Increments the indentation of the generated source code.
     */
    protected void indent() {
        getSourceWriter().indent();
    }

    /**
     * Decrements the indentation of the generated source code.
     */
    protected void outdent() {
        getSourceWriter().outdent();
    }

    /**
     * Prints the given value in the generated source code.
     * 
     * @param value
     *            The value to write.
     */
    protected void print(String value) {
        getSourceWriter().print(value);
    }

    /**
     * Terminates the current line with a new line separator.
     */
    protected void println() {
        getSourceWriter().println();
    }

    /**
     * Prints the given value in the generated source code and terminates the
     * current line with a new line separator.
     * 
     * @param value
     *            The value to write.
     */
    protected void println(String value) {
        getSourceWriter().println(value);
    }

}