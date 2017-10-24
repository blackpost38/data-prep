package org.talend.dataprep.i18n;

import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.i18n.custom.actions.TestAction;

public class ActionsBundleTest {

    public ActionsBundleTest() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void actionLabel() throws Exception {
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(this, Locale.ENGLISH, "negate"));
    }

    @Test
    public void actionLabel_french() throws Exception {
        assertEquals("Négation mathématique de la valeur", ActionsBundle.INSTANCE.actionLabel(this, Locale.FRENCH, "negate"));
    }

    @Test
    public void actionLabel_defaultToEnglish() throws Exception {
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(this, Locale.KOREAN, "negate"));
    }

    @Test(expected = TalendRuntimeException.class)
    public void actionLabel_nonexistentThrowsException() throws Exception {
        assertEquals("toto", ActionsBundle.INSTANCE.actionLabel(this, Locale.US, "toto"));
    }

    @Test
    public void actionDescription() throws Exception {
        assertEquals("Reverse the boolean value of cells from this column",
                ActionsBundle.INSTANCE.actionDescription(this, Locale.ENGLISH, "negate"));
    }

    @Test
    public void emptyActionDocUrl() throws Exception {
        assertEquals("", ActionsBundle.INSTANCE.actionDocUrl(this, Locale.ENGLISH, "uppercase"));
    }

    @Test
    public void parameterLabel() throws Exception {
        assertEquals("Dataset name", ActionsBundle.INSTANCE.parameterLabel(this, Locale.ENGLISH, "name"));
    }

    @Test
    public void parameterDescription() throws Exception {
        assertEquals("Name", ActionsBundle.INSTANCE.parameterDescription(this, Locale.ENGLISH, "name"));
    }

    @Test
    public void choice() throws Exception {
        assertEquals("other", ActionsBundle.INSTANCE.choice(this, Locale.ENGLISH, "custom"));
    }

    @Test
    public void customActionBundleCache() throws Exception {
        assertEquals("Nice custom label", ActionsBundle.INSTANCE.actionLabel(new TestAction(), Locale.ENGLISH, "custom"));
        // Test cache of resource bundle
        assertEquals("Nice custom label", ActionsBundle.INSTANCE.actionLabel(new TestAction(), Locale.ENGLISH, "custom"));
    }

    @Test
    public void customActionLabel() throws Exception {
        assertEquals("Nice custom label", ActionsBundle.INSTANCE.actionLabel(new TestAction(), Locale.ENGLISH, "custom"));
    }

    @Test
    public void customActionDescription() throws Exception {
        assertEquals("Nice custom description",
                ActionsBundle.INSTANCE.actionDescription(new TestAction(), Locale.ENGLISH, "custom"));
    }

    @Test
    public void customActionParameter() throws Exception {
        final TestAction action = new TestAction();
        assertEquals("Nice custom parameter label", action.getParameters(ENGLISH).get(0).getLabel());
        assertEquals("Nice custom parameter description", action.getParameters(ENGLISH).get(0).getDescription());
    }

    @Test
    public void customActionMessageDefaultFallback() throws Exception {
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(new Object(), Locale.ENGLISH, "negate"));
        assertEquals("Negate value", ActionsBundle.INSTANCE.actionLabel(null, Locale.ENGLISH, "negate"));
    }

}
