package commons;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class ColorScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private long serializationId;

    @Getter
    private long id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    @OneToOne(cascade = CascadeType.ALL)
    private Color textColor;

    @Getter @Setter
    @OneToOne(cascade = CascadeType.ALL)
    private Color backgroundColor;

    /**
     * Constructor for ColorScheme
     * @param textColor color for text (called font color in backlog)
     * @param backgroundColor color for background
     */
    public ColorScheme(final Color textColor, final Color backgroundColor) {
        this.id = (long) (Math.random() * 1000000000);
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Constructor for ColorScheme
     * @param id the id of the color scheme
     * @param textColor color for text (called font color in backlog)
     * @param backgroundColor color for background
     */
    public ColorScheme(final long id, final Color textColor, final Color backgroundColor) {
        this.id = id;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Constructor for ColorScheme
     * @param name name of the color scheme
     * @param textColor color for text (called font color in backlog)
     * @param backgroundColor color for background
     */
    public ColorScheme(final String name, final Color textColor, final Color backgroundColor) {
        this.id = (long) (Math.random() * 1000000000);
        this.name = name;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    /**
     * for checkstyle
     */
    public ColorScheme() {
        this.id = (long) (Math.random() * 1000000000);
        this.textColor = new Color(0,0,0,255);
        this.backgroundColor = new Color(0,0,0,255);
    }

    /**
     * Equals method for ColorScheme
     * @param o to compare with this
     * @return this == o ?
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ColorScheme that = (ColorScheme) o;

        return id == that.id;
    }

    /**
     * hashcode for ColorScheme
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * toString for ColorScheme
     * @return name of the color scheme
     */
    @Override
    public String toString() {
        return this.name == null ? "Untitled colorscheme" : this.name;
    }
}
