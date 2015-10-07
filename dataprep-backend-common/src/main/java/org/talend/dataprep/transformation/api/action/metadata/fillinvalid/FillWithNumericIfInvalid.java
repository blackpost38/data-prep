package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

@Component(value = FillWithNumericIfInvalid.ACTION_BEAN_PREFIX + FillWithNumericIfInvalid.FILL_INVALID_ACTION_NAME)
public class FillWithNumericIfInvalid extends AbstractFillIfInvalid {

    public static final String FILL_INVALID_ACTION_NAME = "fillinvalidwithdefaultnumeric"; //$NON-NLS-1$

    @Override
    public String getName() {
        return FILL_INVALID_ACTION_NAME;
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(DEFAULT_VALUE_PARAMETER, ParameterType.INTEGER, "0", false, false)); //$NON-NLS-1$
        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        Type type = Type.get(column.getType());
        return isNumericType(type);
    }

    private boolean isNumericType(Type type) {
        if (Type.NUMERIC.equals(type)) {
            return true;
        }
        Type parent = type.getSuperType();
        if (parent == null) {
            return false;
        }
        return isNumericType(parent);
    }

}
