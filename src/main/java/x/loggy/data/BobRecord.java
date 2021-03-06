package x.loggy.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode
@Table("BOB")
@ToString
public class BobRecord {
    @Id
    public Long id;
    public String name;
}
