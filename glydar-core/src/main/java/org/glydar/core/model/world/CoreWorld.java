package org.glydar.core.model.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.glydar.api.model.entity.Entity;
import org.glydar.api.model.entity.Player;
import org.glydar.api.model.world.World;
import org.glydar.core.model.entity.CoreEntity;
import org.glydar.core.model.entity.CoreEntityData;
import org.glydar.core.model.entity.CorePlayer;
import org.glydar.core.protocol.Packet;
import org.glydar.core.protocol.codec.WorldUpdates;
import org.glydar.core.protocol.packet.Packet00EntityUpdate;
import org.glydar.core.protocol.packet.Packet02UpdateFinished;
import org.glydar.core.protocol.packet.Packet04WorldUpdate;

import com.google.common.collect.Lists;

public class CoreWorld implements World {

	private static long             NEXT_WORLD_ID = 2;
	
	private final long 				id;
    private final String 			name;
    private final int    			seed;
    private boolean      			pvpAllowed;
    private HashMap<Long, Entity> 	entities;
    private final WorldUpdates		updateData;

    public CoreWorld(String name, int seed) {
    	this.id = NEXT_WORLD_ID++;
        this.name = name;
        this.seed = seed;
        this.pvpAllowed = false;
        entities = new HashMap<>();
        updateData = new WorldUpdates();
    }
    
    public List<Entity> getEntities(){
    	return Lists.newArrayList(entities.values());
    }
    
    public List<Player> getPlayers() {
    	List<Player> players = new ArrayList<Player>();
    	for (Entity e : entities.values()){
    		if (e instanceof Player){
    			players.add((Player) e);
    		}
    	}
    	return players;
    }
    
    public Entity getEntityById(long id) {
    	return entities.get(id);
    }
    
    public void unregisterEntity(long id){
    	entities.remove(id);
    }
    
	public void registerEntity(Entity e) {
		entities.put(((CoreEntity) e).getId(), e);
	}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public boolean isPvpAllowed() {
        return pvpAllowed;
    }

    @Override
    public void setPvpAllowed(boolean pvpAllowed) {
        this.pvpAllowed = pvpAllowed;
        // TODO: Update player accordingly
    }
    
    public void sendPacketsToWorld(Packet... packets) {
        for (Player p : getPlayers()){
        	((CorePlayer) p).sendPackets(packets);
        }
    }
    
    public WorldUpdates getUpdateData() {
    	return updateData;
    }
    
    public void tick() {
    	for (Player p :getPlayers()){
    		sendPacketsToWorld(new Packet00EntityUpdate(p.getId(), (CoreEntityData) p.getData()));
    	}
    	sendPacketsToWorld(new Packet02UpdateFinished());
    	
    	if (updateData.hasChanges()){
    		sendPacketsToWorld(new Packet04WorldUpdate(updateData));
    	}
    }
    
}
