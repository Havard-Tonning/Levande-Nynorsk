public class Trie {
    private static final int LETTERS = 29;

    private static class TrieNode{
        TrieNode[] children;
        boolean isLeaf;

        public TrieNode(){
            children = new TrieNode[LETTERS];
            isLeaf = false;

        }
    }

    private TrieNode root;

    public Trie(){
        root = new TrieNode();
    }

    public void insertWord(String word){
        TrieNode curr = root;

        for(char ch : word.toLowerCase().toCharArray()){
            if(ch < 'a' || ch > 'z'){
                continue;
            }

            int index = ch - 'a';
            if(curr.children[index]==null){
                curr.children[index] = new TrieNode();
            }
            curr = curr.children[index];
        }
        curr.isLeaf = true;
    }

    // Search for a word in the trie
    public boolean search(String word) {
        word = word.toLowerCase();

        TrieNode curr = root;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (curr.children[index] == null) {
                return false;
            }
            curr = curr.children[index];
        }
        return curr.isLeaf;
    }

    public void remove(String word) {
        deleteHelper(root, word, 0);
    }

    private boolean deleteHelper(TrieNode node, String word, int depth) {
        if (node == null) {
            return false;
        }

        if (depth == word.length()) {
            if (!node.isLeaf) {
                return false;
            }
            node.isLeaf = false;
            for (int i = 0; i < LETTERS; i++) {
                if (node.children[i] != null) {
                    return false;
                }
            }
            return true;
        }

        int index = word.charAt(depth) - 'a';
        if (deleteHelper(node.children[index], word, depth + 1)) {
            node.children[index] = null;
            return !node.isLeaf && isEmpty(node);
        }
        return false;
    }

    private boolean isEmpty(TrieNode node) {
        for (int i = 0; i < LETTERS; i++) {
            if (node.children[i] != null) {
                return false;
            }
        }
        return true;
    }
}
