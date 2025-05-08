package ogp.multiclass;

import java.awt.Color;
import java.util.ArrayList;

import ogp.game.CollisionInfo;
import ogp.game.Obstacle;
import ogp.game.Player;
import ogp.util.Point;
import ogp.util.Rectangle;
import ogp.util.StringManip;
import logicalcollections.LogicalList;


/**
 * Each instance of this class represents a portal that can be linked to other portals and terminals
 * 
 * @invar | getPluggedPortals() != null
 * @invar | getPluggedPortals().stream().allMatch(p -> p != null)
 * @invar | getExitPortals() != null
 * @invar | getExitPortals().stream().allMatch(p -> p != null)
 * @invar | getLinkedTerminals() != null
 * @invar | getLinkedTerminals().stream().allMatch(p -> p != null)
 * @invar | -1 <= getExitIndex() && getExitIndex() < getExitPortals().size()
 */
public class Portal extends Obstacle implements Connectable {
	
	/**
	 * @invar | id != null
	 */
	String id;
	
	/**
	 * @representationObject
	 * @invar | pluggedPortals != null
	 * @invar | pluggedPortals.stream().allMatch(p -> p != null)
	 */
	ArrayList<Portal> pluggedPortals;
	
	/**
	 * @representationObject
	 * @invar | exitPortals != null
	 * @invar | exitPortals.stream().allMatch(p -> p != null)
	 */
	ArrayList<Portal> exitPortals;
	
	/**
	 * @invar | -1 <= exitIndex && exitIndex < exitPortals.size()
	 */
	int exitIndex;
	
	/**
	 * @representationObject
	 * @peerObjects
	 * @invar | linkedTerminals != null
	 * @invar | linkedTerminals.stream().allMatch(p -> p != null)
	 */
	ArrayList<Terminal> linkedTerminals;
	
	//----getters----
	
	/**
	 * @post | result.equals( StringManip.interleaveHalf( computeTerminalCommands()))
	 */
	public String getId() {
		return id; //LEGIT
	}
	
	/**
	 * @creates | result
	 * @post | result != null
	 * @post | result.stream().allMatch(p -> p != null)
	 * @post | result.equals(new ArrayList<>(result))
	 */
	public ArrayList<Portal> getPluggedPortals() {
		return new ArrayList<>(pluggedPortals); // DAAR WAS ER EEN REPRESENTATION EXPOSURE
	}
	
	/**
	 * @creates | result
	 * @post | result != null
	 * @post | result.stream().allMatch(p -> p != null)
	 * @post | result.equals(new ArrayList<>(result))
	 */
	public ArrayList<Portal> getExitPortals() {
		return new ArrayList<>(exitPortals); // DAAR WAS ER EEN REPRESENTATION EXPOSURE
	}
	
	/**
	 * @post | -1 <= result && result < getExitPortals().size()
	 */
	public int getExitIndex() {
		return exitIndex; //LEGIT
	}
	/**
	 * @peerObjects
	 * @creates | result
	 * @post | result != null
	 * @post | result.stream().allMatch(p -> p != null)
	 * @post | result.equals(new ArrayList<>(result))
	 */
	public ArrayList<Terminal> getLinkedTerminals() {
		return new ArrayList<>(linkedTerminals); // DAAR WAS ER EEN REPRESENTATION EXPOSURE
	}
	
	
	//----constructor----
	
	/**
	 * Initializes this Portal as a Portal whithout a name and connections
	 * 
	 * @post | getHitbox().equals(hitbox)
	 * @post | getId().equals("")
	 * @post | getPluggedPortals().isEmpty()
	 * @post | getExitPortals().isEmpty()
	 * @post | getExitIndex() == -1 
	 * @post | getLinkedTerminals().isEmpty()
	 */
	public Portal(Rectangle hitbox) {
		super(hitbox);
		this.id = "";
		this.pluggedPortals = new ArrayList<>();
		this.exitPortals = new ArrayList<>();
		this.exitIndex = -1;
		this.linkedTerminals = new ArrayList<>();
	}
	
	/**
	 * LEGIT
	 * Constructs a portal with a fixed default hitbox.
	 * Used for testing this multiclass.
	 */
	public Portal() {
		this( new Rectangle( Point.O(), 500,500) );
	}
	
	//----helpers----
	
	/**
	 * LEGIT
	 * list of linked-terminal commands
	 */
	public ArrayList<String> computeTerminalCommands() {
		return computeTerminalCommandsPkg();
	}

	/**
	 * NOSPEC
	 * 
	 * Returns the list of linked-terminal commands.
	 */
	ArrayList<String> computeTerminalCommandsPkg() {
		ArrayList<String> result = new ArrayList<>();
		
		for (Terminal t : getLinkedTerminals()) {
	        result.add(t.getCommand());
	    }
		return result;
	}
	
	/**
	 * Computes the currently correct list of exit portals by iterating over pluggedPortals.
	 * Does not mutate anything.
	 * 
	 * @inspects | pluggedPortals
     * 
     * @creates | result
     * @post | result != null
     * @post | result.stream().allMatch(p -> p != null)
     * @post | result.stream().allMatch(p -> getPluggedPortals().contains(p) &&
     *       | computeTerminalCommands().stream().allMatch(command -> StringManip.containsFirstLast(command, p.getId())))
	 */
	ArrayList<Portal> computeExits() {
		ArrayList<Portal> result = new ArrayList<>();
	    ArrayList<String> commands = computeTerminalCommands();
	    
	    for (Portal plugged : pluggedPortals) {
	        boolean valid = true;
	        for (String command : commands) {
	            if (!StringManip.containsFirstLast(command, plugged.getId())) {
	                valid = false;
	                break;
	            }
	        }
	        if (valid) {
	            result.add(plugged);
	        }
	    }
	    return result;
	}
	
	/**
	 * Computes a value which is currently allowed for exitIndex
	 * @inspects | exitPortals
	 * @inspects | exitIndex
	 * @creates | result
	 * @post | -1 <= result && result < exitPortals.size()
	 */
	int computeExitIndex() {
		//our implem. choses to set exitIndex = exitIndex % exitPortals.size()
		//when a correct value for exitIndex is underspecified (ie can be several ints).
		
		if (exitPortals.isEmpty()) {
			return -1;
		}
		int result = (exitIndex == -1) ? -1 : exitIndex % exitPortals.size();
	    
		return result;
	}
	
	
	
	
	//----mutators
	
	/**
	 * If exitPortals is not empty, increments the index (cycles back to 0 if max index is hit)
	 * Possible:
	 * 	right before calling this method, the index is -1 and exitPortals is not empty.
	 * 	in which case -1 becomes 0.
	 * 
	 * @mutates_properties | getExitIndex(), getLinkedTerminals()
	 * 
	 * @post | getExitPortals().isEmpty() ? getExitIndex() == -1 :
     *       | 0 <= getExitIndex() && getExitIndex() < getExitPortals().size()

	 */
	public void nextIndex() {
				
	    if (exitPortals.isEmpty()) {
	        exitIndex = -1;
	    } else {
	        exitIndex = (exitIndex + 1) % exitPortals.size(); // REGRESSION FLAW
	    }
	    
	    for (Terminal t : linkedTerminals) { // REGRESSION FLAW
	        t.hasWriteAccess = t.computeHasWriteAccess();
	    }
	}


	
	/**
	 * Raises if other == this
	 * plugging twice has no additional effect
	 * 
	 * @pre | other != null
	 * @throws IllegalArgumentException | other == this
	 * 
	 * @mutates_properties | getPluggedPortals(), getExitPortals(), getExitIndex(), other.getPluggedPortals(), other.getExitPortals(), other.getExitIndex()
	 * 
	 * @post | other.getPluggedPortals().equals(LogicalList.plus(old(other.getPluggedPortals()), this))
	 */
	public void plug(Portal other) {
		if (other == this) {
			throw new IllegalArgumentException("A portal cannot be plugged to himself");
			// DAAR WAS ER EEN REGRESSION FLAW
		}
		
		if (!pluggedPortals.contains(other)) { // DAAR WAS ER EEN REGRESSION FLAW
			pluggedPortals.add(other);
			other.pluggedPortals.add(this); // bidirectionele associatie blijft gerespecteerd
			
			// alles updaten voor this en other
			this.exitPortals = computeExits();
			other.exitPortals = other.computeExits();
			this.exitIndex = computeExitIndex();
			other.exitIndex = other.computeExitIndex();
			
			for (Terminal t : linkedTerminals)
				t.hasWriteAccess = t.computeHasWriteAccess();

			for (Terminal t : other.linkedTerminals)
				t.hasWriteAccess = t.computeHasWriteAccess();
			
		}
		
	}
	
	/**
	 * Has no effect if other is not plugged.
	 * Else remove the association between this and other.
	 * @pre | other != null
	 * @throws IllegalArgumentException | other == this
	 * 
	 * @mutates_properties | getPluggedPortals(), getExitPortals(), getExitIndex(), other.getPluggedPortals(), other.getExitPortals(), other.getExitIndex()
	 * 
	 * @post | other.getPluggedPortals().equals(LogicalList.minus(old(other.getPluggedPortals()), this))
	 */
	public void unplug(Portal other) {
		
		if (pluggedPortals.contains(other)) {
			pluggedPortals.remove(other);
			other.pluggedPortals.remove(this);
			
			
			// alles updaten voor this en other
			this.exitPortals = computeExits();
			other.exitPortals = other.computeExits();
			this.exitIndex = computeExitIndex();
			other.exitIndex = other.computeExitIndex();
						
			for (Terminal t : linkedTerminals)
				t.hasWriteAccess = t.computeHasWriteAccess();

			for (Terminal t : other.linkedTerminals)
				t.hasWriteAccess = t.computeHasWriteAccess();
			
			
		}

	}
	
	//integration into the game

	@Override
	/**
	 * LEGIT
	 */
	public String pngPath() {
		return "tiles/portal.png";
	}
	
	@Override
	/**
	 * LEGIT
	 */
	public CollisionInfo handleCollisionPlayer(Player player) {
		return super.handleCollisionPlayer(player);
	}
	
	@Override
	/**
	 * LEGIT
	 */
	public Color paintColor() {
		return Color.ORANGE;
	}
	
	@Override
	/**
	 * LEGIT
	 */
	public boolean isPortal() {
		return true;
	}

	@Override
	/**
	 * @throws IllegalArgumentException | target == null
	 * @throws IllegalArgumentException | target == this
	 * 
	 * @mutates_properties | getPluggedPortals(), getExitPortals(), getExitIndex(),target.getPluggedPortals(), target.getExitPortals(), target.getExitIndex()
	 * 
	 * @post | result == (!old(getPluggedPortals()).contains(target))
	 * @post | getPluggedPortals().contains(target)
	 */
	public boolean establishLink(Portal target) {
		if (target == null)
			throw new IllegalArgumentException("target cannot be null");

		if (target == this || pluggedPortals.contains(target))
			return false;

		this.plug(target);
		return true;
	}
	
	@Override
	/**
	 * LEGIT
	 */
	public Point getCenterPosition() {
		return getHitbox().center();
	}
	
	
}

