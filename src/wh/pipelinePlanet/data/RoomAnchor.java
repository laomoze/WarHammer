package wh.pipelinePlanet.data;

/**
 * 中文说明：房间锚点数据结构，记录位置、半径与类型信息。
 */
public class RoomAnchor{
    public int x;
    public int y;
    public int radius;

    public RoomAnchor(int x, int y, int radius){
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
}
