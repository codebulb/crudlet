package ch.codebulb.crudlet.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Contains all supported query parameter filters and their respective {@link Predicate} factory.
 */
public enum QueryPredicate {
    /** Represents a String equals filter */
    EQ(null) {
        @Override
        protected <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.equal(root.get(attribute), value);
        }
    },
    /** Represents a Long less than or equals filter */
    LE("<") {
        @Override
        protected <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.le(root.get(attribute).as(Number.class), Long.parseLong(value));
        }
    },
    /** Represents a Long greater than or equals filter */
    GE(">") {
        @Override
        protected <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.ge(root.get(attribute).as(Number.class), Long.parseLong(value));
        }
    },
    /** Represents a String SQL "LIKE" filter */
    LIKE("~") {
        @Override
        protected <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.like(root.get(attribute).as(String.class), value);
        }
    },
    /** Represents a Foreign key equals filter */
    ID("Id") {
        @Override
        protected <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.equal(root.get(attribute).get("id"), value);
        }
    };
    
    private final String sign;

    private QueryPredicate(String sign) {
        this.sign = sign;
    }
    
    /**
     * Implements creation of the predicate for the attribute and value provided.
     */
    protected abstract <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value);
    
    /**
     * Creates a predicate for the attribute and value provided. The predicate is chosen by using a special character in either
     * the attribute or the value. Supported special characters are:
     * 
     * <ul>
     * <li><code>value</code> starts with <code>&lt;</code>: {@link #LE}</li>
     * <li><code>value</code> starts with <code>&gt;</code>: {@link #GE}</li>
     * <li><code>value</code> starts with <code>~</code>: {@link #LIKE}</li>
     * <li><code>attribute</code> ends with <code>Id</code>: {@link #ID}</li>
     * </ul>
     * 
     * Otherwise, {@link #EQ} is chosen as the default.
     */
    public static <T> Predicate createPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
        if (value.startsWith(LE.sign)) {
            return LE.create(criteriaBuilder, root, attribute, value.substring(1));
        }
        if (value.startsWith(GE.sign)) {
            return GE.create(criteriaBuilder, root, attribute, value.substring(1));
        }
        if (value.startsWith(LIKE.sign)) {
            return LIKE.create(criteriaBuilder, root, attribute, value.substring(1));
        }
        if (attribute.endsWith(ID.sign)) {
            return ID.create(criteriaBuilder, root, attribute.substring(0, attribute.length() - ID.sign.length()), value);
        }
        return EQ.create(criteriaBuilder, root, attribute, value);
    }
}
