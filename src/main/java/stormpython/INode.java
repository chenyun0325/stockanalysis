package stormpython;

/**
 * Created by chenyun on 15/10/23.
 */
public interface INode {

    //申请资源
    void init();

    //释放资源
    void resourceRelease();

    //启动节点
    void startNode();

    //停止节点
    void stopNode();

}
