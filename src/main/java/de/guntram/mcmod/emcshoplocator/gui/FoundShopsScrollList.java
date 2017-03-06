/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator.gui;

import de.guntram.mcmod.emcshoplocator.ShopSign;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;


/**
 *
 * @author gbl
 */
public class FoundShopsScrollList extends GuiScrollingList {

    private ShopSign[] signs;
    private final Minecraft mc;
    private boolean useSellPrice;
    private int selectedIndex;

    public FoundShopsScrollList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight) {
        super(client, width, height, top, bottom, left, entryHeight);
        this.mc=client;
        this.useSellPrice=false;
        selectedIndex=-1;
    }
    
    public void setSigns(ShopSign[] signs) {
        this.signs=signs;
        resort();
        selectedIndex=-1;
    }
    
    private void resort() {
        Arrays.sort(this.signs, new Comparator<ShopSign>() {
            @Override
            public int compare (ShopSign a, ShopSign b) {
                double result=getChosenPricePerItem(a) - getChosenPricePerItem(b);
                if (result < -0.01)
                    return -1;
                else if (result > 0.01)
                    return 1;
                else
                    return 0;
                
            }
        });
    }
    
    public void setUseSellPrice(boolean b) {
        useSellPrice=b;
        resort();
    }

    private double getChosenPricePerItem(ShopSign sign) {
        if (useSellPrice)
            return sign.getSellPerItem();
        else
            return sign.getBuyPerItem();
    }
    
    private int getChosenPrice(ShopSign sign) {
        if (useSellPrice)
            return sign.getSellPrice();
        else
            return sign.getBuyPrice();
        
    }
    
    @Override
    protected int getSize() {
        return (signs==null ? 0 
                : signs.length < (bottom-top/slotHeight) ? (bottom-top)/slotHeight
                : signs.length);
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        if (signs!=null && index < signs.length)
            selectedIndex=index;
    }

    @Override
    protected boolean isSelected(int index) {
        return selectedIndex==index;
    }

    @Override
    protected void drawBackground() {
        
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        if (signs==null || slotIdx >= signs.length)
                return;
        mc.fontRenderer.drawString(signs[slotIdx].getServer(), this.left+2, slotTop+2, 0xffffff);
        mc.fontRenderer.drawString(String.format("%.2f", getChosenPricePerItem(signs[slotIdx])), this.left+30, slotTop+2, 0xffffff);
        mc.fontRenderer.drawString(Integer.toString(signs[slotIdx].getRes()), this.left+80, slotTop+2, 0xffffff);
        mc.fontRenderer.drawString(signs[slotIdx].getShopOwner(), this.left+130, slotTop+2, 0xffffff);
    }
    
    public ShopSign getSelectedSign() {
        if (signs!=null && selectedIndex<signs.length && selectedIndex>=0)
            return signs[selectedIndex];
        return null;
    }
}