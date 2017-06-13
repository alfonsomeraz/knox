package knox.spring.data.neo4j.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// import org.neo4j.ogm.annotation.*;

import java.util.Stack;



// import com.fasterxml.jackson.annotation.JsonIdentityInfo;
// import com.voodoodyne.jackson.jsog.JSOGGenerator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.neo4j.ogm.annotation.Relationship;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DesignSpace extends NodeSpace {
    String spaceID;
    
    int commitIndex;

    @Relationship(type = "ARCHIVES") Set<Branch> branches;

    @Relationship(type = "SELECTS") Branch headBranch;

    public DesignSpace() {
    	
    }

    public DesignSpace(String spaceID) {
        super(0);

        this.spaceID = spaceID;
        
        commitIndex = 0;
    }

    public DesignSpace(String spaceID, int nodeIndex) {
        super(nodeIndex);

        this.spaceID = spaceID;
        
        commitIndex = 0;
    }

    public void addBranch(Branch branch) {
        if (branches == null) {
            branches = new HashSet<Branch>();
        }
        branches.add(branch);
    }

    public boolean containsCommit(Commit commit) {
        if (hasBranches()) {
            for (Branch branch : branches) {
                if (branch.containsCommit(commit)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    public void copyDesignSpace(DesignSpace space) {
    	copyNodeSpace(space);
    	
    	copyVersionHistory(space);
    }
    
    public void updateCommitIDs() {
    	commitIndex = 0;
    	
    	for (Commit commit : getCommits()) {
    		commit.setCommitID(commitIndex++);
    	}
    }
    
    public Branch copyVersionHistory(DesignSpace space) {
    	Branch headBranchCopy = null;
    	
    	if (space.hasBranches()) {
        	HashMap<String, Commit> idToCommitCopy = new HashMap<String, Commit>();
        	
        	for (Branch branch : space.getBranches()) {
        		Branch branchCopy = branch.copy();
        		
        		if (branch.hasCommits()) {
        			for (Commit commit : branch.getCommits()) {
        				if (!idToCommitCopy.containsKey(commit.getCommitID())) {
        					idToCommitCopy.put(commit.getCommitID(), commit.copy());
        				}
        				
        				branchCopy.addCommit(idToCommitCopy.get(commit.getCommitID()));
        			}
        			
        			for (Commit commit : branch.getCommits()) {
        				if (commit.hasPredecessors()) {
        					for (Commit predecessor : commit.getPredecessors()) {
        						idToCommitCopy.get(commit.getCommitID())
        								.addPredecessor(idToCommitCopy.get(predecessor.getCommitID()));
        					}
        				}
        			}
        			
        			if (idToCommitCopy.containsKey(branch.getLatestCommit().getCommitID())) {
        				branchCopy.setLatestCommit(idToCommitCopy.get(branch.getLatestCommit().getCommitID()));
        			}
        		}
        		
        		addBranch(branchCopy);
        		
        		if (branchCopy.isIdenticalTo(space.getHeadBranch())) {
        			headBranchCopy = branchCopy;
        		}
        	}
        }
    	
    	return headBranchCopy;
    }

    public DesignSpace copy(String copyID) {
    	DesignSpace spaceCopy = new DesignSpace(copyID);

    	spaceCopy.copyNodeSpace(this);
    	
    	spaceCopy.setNodeIndex(nodeIndex);

    	spaceCopy.copyVersionHistory(this);

        return spaceCopy;
    }
    
    public Branch createBranch(String branchID) {
    	Branch branch = new Branch(branchID);
    	
        addBranch(branch);
        
        return branch;
    }

    public Branch createHeadBranch(String branchID) {
        Branch headBranch = createBranch(branchID);
        
        this.headBranch = headBranch;
        
        return headBranch;
    }
    
    public void clearBranches() {
    	branches = null;
    }

    public Branch getBranch(String branchID) {
        if (hasBranches()) {
            for (Branch branch : branches) {
                if (branch.getBranchID().equals(branchID)) {
                    return branch;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    public Set<Branch> getBranches() {
    	return branches; 
    }

    public Branch getHeadBranch() { 
    	return headBranch; 
    }

    public Snapshot getHeadSnapshot() {
    	return headBranch.getLatestCommit().getSnapshot();
    }

    public int getCommitIndex() { 
    	return commitIndex; 
    }
    
    public Commit copyCommit(Branch targetBranch, Commit commit) {
        Commit commitCopy = createCommit(targetBranch);

        commitCopy.copySnapshot(commit.getSnapshot());

        return commitCopy;
    }

    public Commit createCommit(Branch targetBranch) {
        Commit commit = new Commit("c" + commitIndex++);
        
        targetBranch.addCommit(commit);

        return commit;
    }

    public String getSpaceID() { 
    	return spaceID; 
    }

    public void setSpaceID(String spaceID) { 
    	this.spaceID = spaceID; 
    	}

    public boolean hasBranches() {
        if (branches == null) {
            return false;
        } else {
            return branches.size() > 0;
        }
    }

    public void setHeadBranch(Branch headBranch) {
        this.headBranch = headBranch;
    }
    
    public void setBranches(Set<Branch> branches) {
    	this.branches = branches;
    }
    
    public Set<Commit> getCommits() {
    	Set<Commit> commits = new HashSet<Commit>();
    	
    	if (hasBranches()) {
    		for (Branch branch : branches) {
    			Stack<Commit> commitStack = new Stack<Commit>();
    			
    			if (branch.hasCommits()) {
    				commitStack.push(branch.getLatestCommit());

    				while (!commitStack.isEmpty()) {
    					Commit commit = commitStack.pop();

    					commits.add(commit);

    					if (commit.hasPredecessors()) {
    						for (Commit predecessor : commit.getPredecessors()) {
    							commitStack.push(predecessor);
    						}
    					}
    				}
    			}
    		}
    	}
    	
    	return commits;
    }
    
    public boolean isIdenticalTo(DesignSpace space) {
    	return space.getSpaceID().equals(spaceID);
    }
}
