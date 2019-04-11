package io.github.semlink.verbnet;

import com.google.common.base.Strings;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.IMember;
import edu.mit.jverbnet.data.IVerbClass;
import edu.mit.jverbnet.index.IVerbIndex;
import edu.mit.jverbnet.index.VerbIndex;
import io.github.clearwsd.verbnet.VerbNetSenseInventory;
import lombok.Getter;
import lombok.NonNull;

/**
 * Collection of VerbNet classes.
 *
 * @author jgung
 */
public class VerbNet {

    /**
     * Return the base lemma of a multi-word expression/phrasal verb (e.g. "go_ballistic" 0-&gt; "go").
     *
     * @param phrasalVerb multi-word verb
     * @return base lemma
     */
    public static String getBaseForm(String phrasalVerb) {
        String[] fields = phrasalVerb.split("_");
        return fields[0].toLowerCase();
    }

    @Getter
    private IVerbIndex verbnet;

    private ListMultimap<String, VerbNetClass> lemma2Class = LinkedListMultimap.create();

    private Map<String, VerbNetClass> id2Class = new HashMap<>();

    private Map<String, Collection<VerbNetClass>> baseId2Class;

    /**
     * Initialize sense inventory from {@link URL} (possibly on classpath).
     *
     * @param url VerbNet XML URL
     */
    public VerbNet(URL url) {
        verbnet = new VerbIndex(url);
        try {
            verbnet.open();
        } catch (IOException e) {
            throw new RuntimeException("Error loading VerbNet index", e);
        }
        Iterator<IVerbClass> vnIterator = verbnet.iteratorRoots();
        while (vnIterator.hasNext()) {
            VerbNetClass cls = new VerbNetClass(vnIterator.next());

            id2Class.put(cls.id().classId(), cls);

            for (VerbNetClass subcls : VerbNetClass.getAllSubclasses(cls)) {
                for (IMember member : subcls.verbClass().getMembers()) {
                    String name = getBaseForm(member.getName());
                    lemma2Class.put(name, subcls);
                }
            }
        }
    }

    /**
     * Initialize sense inventory with default VerbNet from classpath resources.
     */
    public VerbNet() {
        this(VerbNetSenseInventory.class.getClassLoader().getResource("vn3.3.1.xml"));
    }

    private Map<String, Collection<VerbNetClass>> baseId2Class() {
        if (null != baseId2Class) {
            return baseId2Class;
        }
        ListMultimap<String, VerbNetClass> baseId2Class = LinkedListMultimap.create();
        for (VerbNetClass parent : id2Class.values()) {
            baseId2Class.put(parent.id().baseId(), parent);
        }
        this.baseId2Class = baseId2Class.asMap();
        return this.baseId2Class;
    }

    public List<VerbNetClass> byLemma(@NonNull String lemma) {
        String baseForm = getBaseForm(lemma);
        return lemma2Class.get(baseForm);
    }

    public List<VerbNetClass> parentClasses(@NonNull List<VerbNetClass> classes) {
        return classes.stream().map(VerbNetClass::parentClasses).flatMap(List::stream).distinct().collect(Collectors.toList());
    }

    public List<VerbNetClass> byBaseIdAndLemma(String id, String lemma) {
        if (Strings.isNullOrEmpty(id)) {
            return Collections.emptyList();
        }
        List<VerbNetClass> byLemma = byLemma(lemma);
        if (byLemma.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            VerbNetClass.VerbNetId verbNetId = VerbNetClass.VerbNetId.parse(id);

            Collection<VerbNetClass> verbNetClasses = baseId2Class().get(verbNetId.baseId());
            if (null == verbNetClasses) {
                return Collections.emptyList();
            }
            Set<VerbNetClass> matches = Sets.newHashSet(verbNetClasses);
            return new ArrayList<>(Sets.intersection(matches, new HashSet<>(byLemma)));
        } catch (IllegalArgumentException ignored) {
            // just return empty if class is invalid
        }

        return Collections.emptyList();
    }

    public Optional<VerbNetClass> byId(String id) {
        if (Strings.isNullOrEmpty(id)) {
            return Optional.empty();
        }

        try {
            VerbNetClass.VerbNetId verbNetId = VerbNetClass.VerbNetId.parse(id);

            VerbNetClass rootClass = id2Class.get(verbNetId.rootId());

            if (null == rootClass) {
                return Optional.empty();
            }

            for (VerbNetClass cls : VerbNetClass.getAllSubclasses(rootClass)) {
                if (cls.id().classId().equals(verbNetId.classId())) {
                    return Optional.of(cls);
                }
            }
        } catch (IllegalArgumentException ignored) {
            // just return empty if class is invalid
        }

        return Optional.empty();
    }

    public static void main(String[] args) {
        VerbNet verbNet = new VerbNet();
        System.out.println(verbNet);
    }

}
