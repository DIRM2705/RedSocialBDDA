package com.redsocial.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

@MappedSuperclass
public abstract class Reactionable extends Editable {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reactionable_id") // Esto evita la tabla intermedia indeseada
    protected List<Reaction> reactions = new ArrayList<>();

    public void addReaction(Reaction reaction) {
        this.reactions.add(reaction);
    }

    public void removeReaction(Reaction reaction) {
        this.reactions.remove(reaction);
    }

    public List<Reaction> getReactions() { return reactions; }
    public void setReactions(List<Reaction> reactions) { this.reactions = reactions; }
}
