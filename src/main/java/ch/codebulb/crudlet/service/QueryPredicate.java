package ch.codebulb.crudlet.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public enum QueryPredicate {
    EQ(null) {
        @Override
        public <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.equal(root.get(attribute), value);
        }
    }, LE("<") {
        @Override
        public <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.le(root.get(attribute).as(Number.class), Long.parseLong(value));
        }
    }, GE(">") {
        @Override
        public <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.ge(root.get(attribute).as(Number.class), Long.parseLong(value));
        }
    }, LIKE("~") {
        @Override
        public <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.like(root.get(attribute).as(String.class), value);
        }
    }, ID("Id") {
        @Override
        public <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value) {
            return criteriaBuilder.equal(root.get(attribute).get("id"), value);
        }
    };
    
    private final String sign;

    private QueryPredicate(String sign) {
        this.sign = sign;
    }
    
    public abstract <T> Predicate create(CriteriaBuilder criteriaBuilder, Root<T> root, String attribute, String value);
    
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
