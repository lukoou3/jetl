package com.lk.jetl.sql.expressions;

import com.lk.jetl.sql.analysis.TypeCheckResult;
import com.lk.jetl.sql.types.DataType;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ComplexTypeMergingExpression extends Expression{

    protected List<DataType> getInputTypesForMerging(){
        return getChildren().stream().map(x -> x.getDataType()).collect(Collectors.toList());
    }

    @Override
    public TypeCheckResult checkInputDataTypes() {
        List<DataType> types = getInputTypesForMerging();
        if(types.size() <= 1){
            return TypeCheckResult.typeCheckFailure(String.format("input to function %s requires at least two arguments", prettyName()));
        }

        DataType type = types.get(0);
        for (int i = 1; i < types.size(); i++) {
            if(!types.get(i).sameType(type)){
                return TypeCheckResult.typeCheckFailure("All input types must be the same.The input types found are\\n\\" + types.stream().map(DataType::toString).collect(Collectors.joining("\n\t")));
            }
        }

        return TypeCheckResult.typeCheckSuccess();
    }

    @Override
    public DataType getDataType() {
        return getInputTypesForMerging().get(0);
    }
}
