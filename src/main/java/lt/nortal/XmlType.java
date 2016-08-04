/*
 * Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package lt.nortal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A copy of javax.xml.bind.annotation.XmlType
 *
 * @author Ricardas Buciunas
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface XmlType {
    /**
     * Name of the XML Schema type which the class is mapped.
     */
    String name() default "##default";

    String[] propOrder() default {""};

    /**
     * Name of the target namespace of the XML Schema type. By
     * default, this is the target namespace to which the package
     * containing the class is mapped.
     */
    String namespace() default "##default";

    /**
     * Class containing a no-arg factory method for creating an
     * instance of this class. The default is this class.
     * <p>
     * <p>If <tt>factoryClass</tt> is DEFAULT.class and
     * <tt>factoryMethod</tt> is "", then there is no static factory
     * method.
     * <p>
     * <p>If <tt>factoryClass</tt> is DEFAULT.class and
     * <tt>factoryMethod</tt> is not "", then
     * <tt>factoryMethod</tt> is the name of a static factory method
     * in this class.
     * <p>
     * <p>If <tt>factoryClass</tt> is not DEFAULT.class, then
     * <tt>factoryMethod</tt> must not be "" and must be the name of
     * a static factory method specified in <tt>factoryClass</tt>.
     */
    Class factoryClass() default DEFAULT.class;

    /**
     * Used in {@link XmlType#factoryClass()} to
     * signal that either factory mehod is not used or
     * that it's in the class with this {@link XmlType} itself.
     */
    static final class DEFAULT {}

    /**
     * Name of a no-arg factory method in the class specified in
     * <tt>factoryClass</tt> factoryClass().
     */
    String factoryMethod() default "";
}
