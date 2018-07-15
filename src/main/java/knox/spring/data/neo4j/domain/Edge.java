package knox.spring.data.neo4j.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@RelationshipEntity(type = "PRECEDES")
public class Edge {
    @GraphId Long id;

    @StartNode Node tail;

    @EndNode Node head;

    ArrayList<String> componentIDs;
    
    ArrayList<String> componentRoles;
    
    String orientation;

    double weight;

    public Edge() {
    	
    }

    public Edge(Node tail, Node head) {
        this.tail = tail;
        
        this.head = head;
        
        componentIDs = new ArrayList<String>();
        
        componentRoles = new ArrayList<String>();
        
        orientation = Orientation.NONE.getValue();
        
        weight = 1.0;
    }
    
    public Edge(Node tail, Node head, ArrayList<String> componentIDs, 
    		ArrayList<String> componentRoles) {
		this.tail = tail;
		
		this.head = head;
		
		this.componentIDs = componentIDs;
		
		this.componentRoles = componentRoles;
		
		if (!this.componentIDs.isEmpty() || !this.componentRoles.isEmpty()) {
			orientation = Orientation.INLINE.getValue();
		} else {
			orientation = Orientation.NONE.getValue();
		}
		
        weight = 1.0;
	}
    
    public Edge(Node tail, Node head, ArrayList<String> componentIDs, 
    		ArrayList<String> componentRoles, String orientation) {
		this.tail = tail;
		
		this.head = head;
		
		this.componentIDs = componentIDs;
		
		this.componentRoles = componentRoles;
		
		if (!this.componentIDs.isEmpty() || !this.componentRoles.isEmpty()) {
			this.orientation = orientation;
		} else {
			this.orientation = Orientation.NONE.getValue();
		}
        
        weight = 1.0;
	}
    
    public Edge(Node tail, Node head, ArrayList<String> componentIDs, 
    		ArrayList<String> componentRoles, String orientation, double weight) {
    	this.tail = tail;

    	this.head = head;

    	this.componentIDs = componentIDs;

    	this.componentRoles = componentRoles;

    	if (!this.componentIDs.isEmpty() || !this.componentRoles.isEmpty()) {
			this.orientation = orientation;
		} else {
			this.orientation = Orientation.NONE.getValue();
		}

    	this.weight = weight;
    }
    
    public Edge(ArrayList<String> componentIDs, ArrayList<String> componentRoles, String orientation, 
    		double weight) {
    	this.componentIDs = componentIDs;

    	this.componentRoles = componentRoles;

    	if (!this.componentIDs.isEmpty() || !this.componentRoles.isEmpty()) {
			this.orientation = orientation;
		} else {
			this.orientation = Orientation.NONE.getValue();
		}

    	this.weight = weight;
    }
    
    public Edge copy() {
    	return new Edge(new ArrayList<String>(componentIDs),
    			new ArrayList<String>(componentRoles), orientation, weight);
    }
    
    public Edge copy(Node head) {
    	return new Edge(tail, head, new ArrayList<String>(componentIDs),
    			new ArrayList<String>(componentRoles), orientation, weight);
    }

    public Edge copy(Node tail, Node head) {
    	return new Edge(tail, head, new ArrayList<String>(componentIDs),
    			new ArrayList<String>(componentRoles), orientation, weight);
    }
    
    public void delete() {
    	tail.deleteEdge(this);
    }
    
    public void diffWithEdge(Edge edge) {
    	componentIDs.removeAll(edge.getComponentIDs());
    }

    public Node getTail() {
    	return tail; 
    }

    public Node getHead() { 
    	return head; 
    }
    
    public String getTailID() {
    	return tail.getNodeID();
    }
    
    public String getHeadID() {
    	return head.getNodeID();
    }
    
    public ArrayList<String> getComponentIDs() {
    	return componentIDs; 
    }

    public ArrayList<String> getComponentRoles() { 
    	return componentRoles; 
    }

    public boolean hasComponentIDs() {
    	return componentIDs != null && !componentIDs.isEmpty();
    }

    public boolean hasRole(Set<String> roles) {
    	if (roles.isEmpty()) {
    		return true;
    	} else {
    		Set<String> compRoles = new HashSet<String>(componentRoles);

    		compRoles.retainAll(roles);

    		return !compRoles.isEmpty();
    	}
    }

    public boolean hasComponentRoles() {
    	return componentRoles != null && !componentRoles.isEmpty();
    }
    
    public boolean hasOrientation() {
    	return isInline() || isReverseComplement();
    }
    
    public boolean hasOrientation(String orientation) {
    	return hasOrientation() && this.orientation.equals(orientation);
    }

    public void intersectWithEdge(Edge edge) {
    	componentIDs.retainAll(edge.getComponentIDs());

    	componentRoles.retainAll(edge.getComponentRoles());
    }
    
    public boolean isMatching(Edge edge, int tolerance, Set<String> roles) {
    	return hasSameOrientation(edge) 
    			&& (tolerance == 0 && hasSameComponentIDs(edge) && hasSameRoles(edge, roles)
    					|| (tolerance == 1 || tolerance == 2) && hasSharedComponentIDs(edge) && hasSharedRoles(edge, roles)
    					|| tolerance == 2 && hasSameRoles(edge, roles)
    					|| tolerance == 3 && hasSharedRoles(edge, roles));
    }
    
    public boolean hasSameOrientation(Edge edge) {
    	return hasOrientation(edge.getOrientation());
    }
    
    public boolean hasSameComponentIDs(Edge edge) {
    	 if (hasComponentIDs() && edge.hasComponentIDs()) {
             Set<String> compIDs1 = new HashSet<String>(componentIDs);
             
             Set<String> compIDs2 = new HashSet<String>(edge.getComponentIDs());

             return compIDs1.equals(compIDs2);
         } else {
             return !hasComponentIDs() && !edge.hasComponentIDs();
         }
    }

    public boolean hasSharedComponentIDs(Edge edge) {
   	 if (hasComponentIDs() && edge.hasComponentIDs()) {
            Set<String> compIDs1 = new HashSet<String>(componentIDs);
            
            Set<String> compIDs2 = new HashSet<String>(edge.getComponentIDs());

            compIDs1.retainAll(compIDs2);

            return !compIDs1.isEmpty();
        } else {
        	return !hasComponentIDs() && !edge.hasComponentIDs();
        }
   }
    
    public boolean hasSameRoles(Edge edge, Set<String> roles) {
    	if (hasComponentRoles() && edge.hasComponentRoles()) {
    		Set<String> compRoles1 = new HashSet<String>(componentRoles);

    		Set<String> compRoles2 = new HashSet<String>(edge.getComponentRoles());

    		return compRoles1.equals(compRoles2) && hasRole(roles);
    	} else {
    		return !hasComponentRoles() && !edge.hasComponentRoles() && roles.isEmpty();
        }
    }

    public boolean hasSharedRoles(Edge edge, Set<String> roles) {
    	if (hasComponentRoles() && edge.hasComponentRoles()) {
    			Set<String> compRoles1 = new HashSet<String>(componentRoles);
    			
    			Set<String> compRoles2 = new HashSet<String>(edge.getComponentRoles());

    			compRoles1.retainAll(compRoles2);
    			
    			if (!roles.isEmpty()) {
    				compRoles1.retainAll(roles);
    			}

    			return !compRoles1.isEmpty();
    	} else {
    		return !hasComponentRoles() && !edge.hasComponentRoles() && roles.isEmpty();
    	}
    }
    
    public boolean isInline() {
    	return orientation.equals(Orientation.INLINE.getValue());
    }
    
    public boolean isReverseComplement() {
    	return orientation.equals(Orientation.REVERSE_COMPLEMENT.getValue());
    }
    
    public boolean isBlank() {
    	return !hasComponentIDs() && !hasComponentRoles() && !hasOrientation();
    }
    
    public void setComponentIDs(ArrayList<String> compIDs) {
    	componentIDs = compIDs;
    }

    public void setHead(Node head) {
    	this.head = head; 
    }

    public void setTail(Node tail) { 
    	this.tail = tail; 
    }

    public void unionWithEdge(Edge edge) {
        Set<String> unionedIDs = new HashSet<String>();
        
        unionedIDs.addAll(componentIDs);

        for (String compID : edge.getComponentIDs()) {
        	if (!unionedIDs.contains(compID)) {
        		componentIDs.add(compID);

        		unionedIDs.add(compID);
        	}
        }
        
        componentRoles.retainAll(edge.getComponentRoles());
    }
    
    public String getOrientation() {
    	return orientation;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() { 
    	return weight; 
    }
    
    public enum Orientation {
        INLINE("inline"),
        REVERSE_COMPLEMENT("reverseComplement"),
        NONE("none");

        private final String value;
        
        Orientation(String value) { 
        	this.value = value;
        }

        public String getValue() {
        	return value; 
        }
    }
}
