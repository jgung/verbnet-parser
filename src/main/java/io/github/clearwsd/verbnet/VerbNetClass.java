package io.github.clearwsd.verbnet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.IVerbClass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Wrapper for {@link IVerbClass}.
 *
 * @author jgung
 */
@EqualsAndHashCode(of = "id")
@Accessors(fluent = true)
public class VerbNetClass {

    @Getter
    private IVerbClass verbClass;
    @Getter
    private VerbNetId id;

    @Getter
    private VerbNetClass parent;
    @Getter
    private List<VerbNetClass> children;

    public VerbNetClass(IVerbClass cls) {
        verbClass = cls;
        id = new VerbNetId(cls.getID());
        // create classes for children
        children = cls.getSubclasses().stream()
                .map(child -> {
                    VerbNetClass childClass = new VerbNetClass(child);
                    childClass.parent = this;
                    return childClass;
                })
                .collect(Collectors.toList());
    }

    @Data
    @Accessors(fluent = true)
    public static class VerbNetId {

        public static final Pattern VN_ID_PATTERN = Pattern.compile(
                "(?:(?<name>[a-zA-Z-_]+)-)?(?<cls>(?<id>(?<baseId>\\d+)(?:\\.\\d+)*)(?<subcls>(?:-\\d+)*))");

        private String classId;
        private String name;
        private String rootId;
        private String baseId;

        VerbNetId(String id) {
            Matcher matcher = VN_ID_PATTERN.matcher(id);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Invalid VerbNet class ID: " + id);
            }
            name = matcher.group("name");
            rootId = matcher.group("id");
            classId = matcher.group("cls");
            baseId = matcher.group("baseId");
        }

        public static VerbNetId parse(@NonNull String id) {
            return new VerbNetId(id);
        }

    }

    /**
     * Returns all children and parents of this class.
     */
    public List<VerbNetClass> relatedClasses() {
        return getAllSubclasses(getRoot(this));
    }

    public List<VerbNetClass> parentClasses() {
        List<VerbNetClass> parents = new ArrayList<>();
        VerbNetClass parent = this;

        parents.add(this);

        while (parent.parent() != null) {
            parents.add(parent.parent());
            parent = parent.parent();
        }
        return parents;
    }

    public List<VerbNetClass> subClasses() {
        return getAllSubclasses(this);
    }

    public static VerbNetClass getRoot(@NonNull VerbNetClass sub) {
        while (sub.parent() != null) {
            sub = sub.parent();
        }
        return sub;
    }

    public static List<VerbNetClass> getAllSubclasses(@NonNull VerbNetClass parent) {
        List<VerbNetClass> subclasses = new ArrayList<>();
        subclasses.add(parent);
        for (VerbNetClass sub : parent.children()) {
            subclasses.addAll(getAllSubclasses(sub));
        }
        return subclasses;
    }

    @Override
    public String toString() {
        return verbClass.getID();
    }
}
