/*
*   name: youssef mohamed torki ahmed
*   id: 445820246
*   project2: text replacement using BST
*   
*   techinal description
*   
*   class BSNode
*   this class represents a single element in the text tree.
*   a single node contains
*   1- a single word of the text
*   2- a list to track the frequency of the word and the positions where it's encountered.
*       this way the order is preserved when inserting the word into the tree.
*   3- a pointer to left and right children
*
*   class BSTree
*   this class represents a BST structure that inserts words into the tree using lexicographical order
*   meaning that each character in the string is compared according to thier index in the alphabet or the ASCII table
*   
*   the main class reads the file character by character until a non alphabitecal character is encountered.
*   the word is inserted into the tree, the non-alphabetecial character is added as a seperate element,
*   the counter is incremented to represent the current position each time an element is added
*   we also keep track of the current number of positions in the tree to reconstruct the string later
*   when we need to replace a word with another word:
*   1- we search the tree using BST properties and lexicographical ordering
*   2- if the word is found return the containing node
*   3- overwrite the text content of the node
*   4- the original position of the word is still preserved
*   
*   when we want to reconstruct the text:
*   1- make an array the size of the text positions
*   2- traverse the tree and fill the array with the text at the index corresponding to the current occurence list entry.
*   3- return the result as a whole string
*   
*   and then the result is written to the output file.
*/

import java.util.*;
import java.io.*;

public class Project2 {
    
    public static void main(String[] args) {
        
        try{
            File inputFile = new File("input.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            BSTree btree = new BSTree();

            int x;
            StringBuilder words = new StringBuilder();
            int pos = 0;
            while ((x = reader.read()) != -1) {
                
                if (Character.isLetterOrDigit(x)) {
                    words.append( (char) x);
                } else {
                    // Add word if we have one
                    if (words.length() > 0) {
                        btree.printDebug("Adding word: '" + words.toString() + "' to the tree.");
                        btree.insert(words.toString(), pos++);
                        words = new StringBuilder();
                    }

                    if (!Character.isWhitespace(x) || x == ' ' || x == '\n') {
                        btree.insert(String.valueOf( (char) x), pos++);
                    }
                }
            }
            // Don't forget the last word
            if (words.length() > 0) {
                btree.insert(words.toString(), pos++);
            }
            
            reader.close();
            btree.printOccurence();
            System.out.println("Total positions: " + btree.positionCount);
            
            Scanner in = new Scanner(System.in);
            System.out.print("Enter the word to search for: ");
            String target = in.nextLine();
            System.out.print("Enter replacement word: ");
            String replacement = in.nextLine();

            btree.replace(target, replacement);

            String result = btree.formString();
            System.out.println("Result: " + result);
            
            FileWriter writer = new FileWriter("output.txt");
            writer.write(result);
            writer.close();
            
            in.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}

class BSNode {
    String text;
    // we need to preserve text order. a thing BSTs are not good at. so we have to help a little :)
    List<Integer> occurenceList = new ArrayList<>();
    BSNode left, right;

    public BSNode(String txt) {
        this.text = txt;
        left = right = null;
    }

    public void addOccurence(int pos) {
        occurenceList.add(pos);
    }
}

class BSTree {
    public BSNode root;
    boolean debugMode = true;
    int positionCount = 0;
    
    public BSTree() {
        root = null;
    }

    public void insert(String txt, int position) {
        root = insertNode(root, txt, position);
        positionCount = position + 1;
    }

    private BSNode insertNode(BSNode root, String txt, int pos) {
        if (root == null) {
            BSNode newNode = new BSNode(txt);
            newNode.addOccurence(pos);
            return newNode;
        }

        int result = txt.compareTo(root.text);
        
        // Word already exists. just add the position
        if (result == 0) {
            root.addOccurence(pos);
            // The given text is bigger than root (lexicographically)
        } else if (result > 0) {
            root.right = insertNode(root.right, txt, pos);
        } else if (result < 0) {
            // The given text is smaller than root
            root.left = insertNode(root.left, txt, pos);
        }
        
        return root;
    }

    public BSNode searchWord(String word) {
        return searchWord(word, root);
    }

    private BSNode searchWord(String word, BSNode root) {
        if (root == null) return null; // tree traversed but word not found :(
        
        int result = word.compareTo(root.text);
        if (result == 0) return root;
        else if (result > 0) return searchWord(word, root.right);
        else if (result < 0) return searchWord(word, root.left);

        else return null; // just so the compiler wouldn't complain.
    }

    public void replace(String target, String replacement) {
        BSNode targetNode = searchWord(target);
        
        if (targetNode == null) {
            System.out.println("Word '" + target + "' not found.");
            return;
        } else {
            targetNode.text = replacement;
        }
    }
    
    public void printDebug(String txt) {
        if (debugMode) System.out.println(txt);
    }

    public void printOccurence() {
        occurencePrinter(root);
    }

    public String formString() {

        String[] pieces = new String[positionCount];
        
        // Fill the array by traversing the tree
        fillPositionArray(root, pieces);
        

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < positionCount; i++) {
            if (pieces[i] != null) {
                result.append(pieces[i]);
            }
        }
        
        return result.toString();
    }
    
    private void fillPositionArray(BSNode node, String[] pieces) {
        if (node != null) {

            fillPositionArray(node.left, pieces);
            
            // Process current node
            for (int pos : node.occurenceList) {
                if (pos < pieces.length) {
                    pieces[pos] = node.text;
                }
            }
            

            fillPositionArray(node.right, pieces);
        }
    }

    private void occurencePrinter(BSNode root) {
        if (root != null) {
            occurencePrinter(root.left);
            System.out.println("Word: '" + root.text + "' occurs at positions: " + root.occurenceList);
            occurencePrinter(root.right);
        }
    }
}