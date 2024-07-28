package com.lk.jetl.sql.analysis;

public interface TypeCheckResult {
    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    static TypeCheckSuccess typeCheckSuccess = new TypeCheckSuccess();

    public static TypeCheckSuccess typeCheckSuccess(){
        return typeCheckSuccess;
    }

    public static TypeCheckFailure typeCheckFailure(String message){
        return new TypeCheckFailure(message);
    }

    public static class TypeCheckSuccess implements TypeCheckResult{

        TypeCheckSuccess() {
        }

        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    public static class TypeCheckFailure implements TypeCheckResult{
        public final String message;

        TypeCheckFailure(String message) {
            this.message = message;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
    }
}

