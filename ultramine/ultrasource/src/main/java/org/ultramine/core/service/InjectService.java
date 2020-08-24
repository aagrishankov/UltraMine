package org.ultramine.core.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For any annotated field bytecode transformation will be applied for the corresponding service provider injection.
 * The field should be declared as static and non-final. After transformation field become final. Example: <br />
 * <pre><code>
 *    {@literal @}InjectService
 *     private static SomeService service;
 * </code></pre>
 * Will be transformed to: <br />
 * <pre><code>
 *     private static final SomeService service = (SomeService) ServiceBytecodeAdapter.provideService(SomeService.class);
 * </code></pre>
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface InjectService
{
}
