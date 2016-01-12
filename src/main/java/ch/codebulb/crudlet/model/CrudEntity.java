package ch.codebulb.crudlet.model;

import ch.codebulb.crudlet.service.CrudService;
import ch.codebulb.crudlet.webservice.CrudResource;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * <p>
 * An abstract generic base class for a persistent business entity (model).
 * </p>
 * <p>
 * Use either the {@link CrudIdentifiable} interface or the
 * <code>CrudEntity</code> class to derive your entity model classes from. This
 * is the only prerequisite to use them with a {@link CrudService} and a
 * {@link CrudResource}.</p>
 * <p>
 * The difference between the interface and the class is that the latter
 * provides an auto-generated <code>Long id</code> field implementation
 * out-of-the-box.</p>
 * <p>
 * As an example, a <code>Customer</code> entity can be created by
 * deriving from
 * <code>CrudEntity</code> like so:</p>
 * <pre class="brush:java">
&#064;Entity
public class Customer extends CrudEntity { 
    &#064;NotNull
    &#064;Pattern(regexp = "[A-Za-z ]*")
    private String name;
    private String address;
    private String city;
    ...
}
</pre>
 * <p>
 * Apart from the <code>&#064;Entity</code> annotation, this class consists
 * solely of business logic.</p>
 */
// as in https://github.com/codebulb/crudfaces/blob/master/src/main/java/ch/codebulb/crudfaces/model/CrudEntity.java
@MappedSuperclass
public abstract class CrudEntity implements CrudIdentifiable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CrudIdentifiable)) {
            return false;
        }
        CrudIdentifiable other = (CrudIdentifiable) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[ id=" + id + " ]";
    }
}
