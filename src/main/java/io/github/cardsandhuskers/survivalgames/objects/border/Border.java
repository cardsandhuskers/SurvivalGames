package io.github.cardsandhuskers.survivalgames.objects;

public interface Border {
    /**
     * Initializes a worldborder the size of the arena
     * @param centerX X coordinate of center
     * @param centerZ Z coordinate of center
     */
    void buildWorldBorder(int centerX, int centerZ);
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

    void updateSizeVariable();


}
