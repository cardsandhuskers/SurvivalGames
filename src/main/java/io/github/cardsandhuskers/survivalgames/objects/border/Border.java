package io.github.cardsandhuskers.survivalgames.objects.border;

public interface Border {

    /**
     * Initializes a worldborder the size of the arena
     */
    void buildWorldBorder();
    /**
     * Resizes the worldborder
     * @param size end size of border
     * @param time time for shrink
     */
    void shrinkWorldBorder(int size, int time);

    /**
     * Sets the border size variable (static int borderSize)
     */
    void setSize(int size);

    void startOperation();
    void cancelOperation();
    int getCenterX();
    int getCenterZ();


}
