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
     * Instantly sets world border to the specified size
     * @param size
     */
    void setWorldBorder(int size);

    /**
     * Updates the border size variable (static int borderSize)
     */
    void updateSizeVariable();


}
