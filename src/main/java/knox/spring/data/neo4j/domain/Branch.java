package knox.spring.data.neo4j.domain;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@NodeEntity
public class Branch {
    @GraphId Long id;

    String branchID;

    @Relationship(type = "CONTAINS") Set<Commit> commits;

    @Relationship(type = "LATEST") Commit latestCommit;

    public Branch() {}
    
    public Branch(String branchID) {
        this.branchID = branchID;
        
        commits = new HashSet<Commit>();
    }

    public void addCommit(Commit commit) {
        commits.add(commit);
    }

    public boolean containsCommit(Commit commit) {
    	return commits.contains(commit);
    }
    
    public Branch copy() {
    	return new Branch(branchID);
    }

    public boolean deleteCommits(Set<Commit> deletedCommits) {
    	return commits.removeAll(deletedCommits);
    }

    public Set<Commit> getCommits() { 
    	return commits; 
    }
    
    public int getNumCommits() {
    	return commits.size();
    }

    public void setCommits(Set<Commit> commits) {
    	this.commits = commits;
    }
    
    public Commit getLatestCommit() { 
    	return latestCommit; 
    }
    
    public void clearLatestCommit() {
    	latestCommit = null;
    }
    
    public void clearCommits() {
    	commits.clear();
    }

    public String getBranchID() { 
    	return branchID; 
    }
    
    public boolean hasLatestCommit() {
    	return latestCommit != null;
    }

    public boolean hasCommits() {
    	return commits != null && !commits.isEmpty();
    }

    public Set<Commit> retainCommits(Set<Commit> retainedCommits) {
        Set<Commit> diffCommits = new HashSet<Commit>();
        
        for (Commit commit : commits) {
        	if (!retainedCommits.contains(commit)) {
        		diffCommits.add(commit);
        	}
        }

        deleteCommits(diffCommits);

        return diffCommits;
    }
    
    public boolean isIdenticalTo(Branch branch) {
    	return branch.getBranchID().equals(branchID);
    }

    public void setLatestCommit(Commit commit) { 
    	latestCommit = commit; 
    }
}
