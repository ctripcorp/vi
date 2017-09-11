package com.ctrip.framework.vi.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiang.j on 2016/8/24.
 */
public class DepSortUtil {

    public static class Node{
        private String _id;
        private String[] _before,_afer;
        public Node(String id,String[] before,String[] after){
            _id = id;
            _before = before;
            _afer = after;
        }

        public String id(){
            return _id;
        }

        public String[] before(){
            return _before;
        }
        public String[] after(){
            return  _afer;
        }
    }

    /**
     * sort node list by their dependencies.
     * this operation have side affection, it would empty the input list.
     * @param nodes need sorted nodes
     * @return
     * @throws LoopReferenceNodeException
     */
    public static List<String> sort(List<Node> nodes) throws LoopReferenceNodeException {

        List<String> sortedIds = new ArrayList<>();
        while (!nodes.isEmpty()) {
            Node first = null;
            for (Node info : nodes) {
                if (!hasBefore(info, nodes, sortedIds)) {
                    first = info;
                    break;
                }
            }
            if (first != null) {
                sortedIds.add(first.id());
                nodes.remove(first);
            } else if (!nodes.isEmpty()) {
                String[] ids = new String[nodes.size()];
                int i=0;
                for(Node n:nodes){
                    ids[i++] = n.id();
                }
                throw new LoopReferenceNodeException(ids);
            }

        }

        return sortedIds;
    }

    private static boolean hasBefore(Node currentInfo,List<Node> nodes,List<String> sortedIds){

        for(String aId:currentInfo.after()){
            if(!sortedIds.contains(aId)){
                return true;
            }
        }
        String id = currentInfo.id();
        for(Node info:nodes){
            if(id.equals(info.id()) || sortedIds.contains(info.id())){
                continue;
            }
            if(ArrayUtils.contains(info.before(),id) && !ArrayUtils.contains(info.after(),id)){
                return true;
            }
        }
        return false;
    }
}
