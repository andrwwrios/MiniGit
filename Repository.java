// andrew rios 
// ta : Shreya Nambi
// 05/01/2024
// P1: Mini-Git

/*
 * This class represents a set of documents and their histories are referred to as a repository.
 * Each revision within a repository is referred to as a Commit which is represented by 
 * another class inside this program. This program supports a subset of the operations 
 * supported by real Git repositories such as creating and dropping commits and synchronyzing
 * repositories. 
 */

 import java.util.*;
 import java.text.SimpleDateFormat;
 
 public class Repository {
     private Commit head;
     private String name;
     private int size;
 
     // Creates a new, empty repository with the specified name
     // If the name is null or empty, throw an IllegalArgumentException
     public Repository(String name) {
         if(name == null || name.isEmpty()) {
             throw new IllegalArgumentException();
         }
         this.head = null;
         this.size = 0;
         this.name = name;
     }
 
     // Return the ID of the current head of this repository
     // If the head is null, returns null
     public String getRepoHead () {
         if(head == null) {
             return null;
         }
         return head.id;
     }
 
     // Return the number of commits in this repository
     public int getRepoSize() {
         return size;
     }
 
     // Return a string representation of this repository containing the 
     // name of this repo and the current commit head
     // if this Repo is empty the representation reflects that
     public String toString() {
         if(size == 0) {
             return name + " - No commits";
         }
         return name + " - Current head: " + head.toString();
     }
 
     // Return true if a commit with Id targetId is in the repository, false if not.
     public boolean contains(String targetId) {
         Commit temp = head;
         while(temp != null) {
             if(temp.id.equals(targetId)) {
                 return true;
             }
             temp = temp.past;
         }
         return false;
     }
 
     // Return a string consisting of the String representations of the most recent n commits in 
     // this repository, with the most recent first
     // if this repo is empty, returns an empty string
     // if n is non positive throws an IllegalArgumentException
     // if n is greater than the current amount of commits in the repo, it returns all the commits
     public String getHistory(int n) {
         if(n < 1) {
             throw new IllegalArgumentException();
         }
         String result = "";
         Commit temp = head;
         int count = 0;
         while(count < n && temp != null) {
             result += temp + "\n";
             temp = temp.past;
             count ++;
         }
         return result;
     }
     
     // Creates a new commit with the given message, adds it to this repository 
     // preserving the history behind it
     // returns the id of the new commit
     public String commit(String message) {
         Commit past = head;
         head = new Commit(message, past);
         size ++;
         return head.id;
     }
 
     // Remove the commit with ID targetId from this repository, maintaining the rest of the history
     // Returns true if the commit was successfully dropped, and false if there is no commit that 
     // matches the given ID in the repository.
     public boolean drop(String targetId) {
         if(!contains(targetId)) {
             return false;
         }
         
         if(head.id.equals(targetId)) {
             head = head.past;
             size--;
             return true;
         }
 
         Commit temp = head;
         while(!temp.past.id.equals(targetId)) {
             temp = temp.past;
         }
         temp.past = temp.past.past;
         size--;
         return true; 
     }
 
     // Takes all the commits in the other repository and moves them into this repository,
     // combining the two repository histories such that chronological order is preserved
     public void synchronize(Repository other) {
         if(head == null) {
             head = other.head;
         } else if(other.head != null){
             Commit temp = head;
             Commit curr = head;
 
             if(other.head.timeStamp > head.timeStamp) {
                 head = other.head;
                 curr = other.head;
                 other.head = other.head.past;
                 head.past = temp;
             } else if (temp.past == null) {
                 temp = other.head;
                 curr.past = temp;
                 temp = null;
             } 
             
             while(temp != null && other.head != null) {
                 if(other.head.timeStamp > temp.timeStamp) {
                     curr.past = other.head;
                     curr = other.head;
                     other.head = other.head.past;
                     curr.past = temp;
                 } else {
                     curr = temp;
                     temp = temp.past;
                 }
             }
         }
         other.head = null;
         this.size += other.size;
         other.size = 0;
     }
 
     /**
      * DO NOT MODIFY
      * A class that represents a single commit in the repository.
      * Commits are characterized by an identifier, a commit message,
      * and the time that the commit was made. A commit also stores
      * a reference to the immediately previous commit if it exists.
      *
      * Staff Note: You may notice that the comments in this 
      * class openly mention the fields of the class. This is fine 
      * because the fields of the Commit class are public. In general, 
      * be careful about revealing implementation details!
      */
     public class Commit {
 
         private static int currentCommitID;
 
         /**
          * The time, in milliseconds, at which this commit was created.
          */
         public final long timeStamp;
 
         /**
          * A unique identifier for this commit.
          */
         public final String id;
 
         /**
          * A message describing the changes made in this commit.
          */
         public final String message;
 
         /**
          * A reference to the previous commit, if it exists. Otherwise, null.
          */
         public Commit past;
 
         /**
          * Constructs a commit object. The unique identifier and timestamp
          * are automatically generated.
          * @param message A message describing the changes made in this commit.
          * @param past A reference to the commit made immediately before this
          *             commit.
          */
         public Commit(String message, Commit past) {
             this.id = "" + currentCommitID++;
             this.message = message;
             this.timeStamp = System.currentTimeMillis();
             this.past = past;
         }
 
         /**
          * Constructs a commit object with no previous commit. The unique
          * identifier and timestamp are automatically generated.
          * @param message A message describing the changes made in this commit.
          */
         public Commit(String message) {
             this(message, null);
         }
 
         /**
          * Returns a string representation of this commit. The string
          * representation consists of this commit's unique identifier,
          * timestamp, and message, in the following form:
          *      "[identifier] at [timestamp]: [message]"
          * @return The string representation of this collection.
          */
         @Override
         public String toString() {
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
             Date date = new Date(timeStamp);
 
             return id + " at " + formatter.format(date) + ": " + message;
         }
 
         /**
         * Resets the IDs of the commit nodes such that they reset to 0.
         * Primarily for testing purposes.
         */
         public static void resetIds() {
             Commit.currentCommitID = 0;
         }
     }
 }