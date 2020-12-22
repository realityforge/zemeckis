package zemeckis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation use to mark code as incompatible with GWT.
 * The Name of the annotation is all that matters.
 */
@Retention( RetentionPolicy.SOURCE )
@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD } )
@interface GwtIncompatible
{
}
