package com.science.gtnl.common.machine.multiblock.AprilFool;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.GregTechAPI.*;
import static gregtech.api.util.GTStructureUtility.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.science.gtnl.common.machine.multiMachineClasses.SteamMultiMachineBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.OverclockCalculator;
import gregtech.common.blocks.BlockCasings1;
import gregtech.common.blocks.BlockCasings2;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class SteamRockBreaker extends SteamMultiMachineBase<SteamRockBreaker> implements ISurvivalConstructable {

    public SteamRockBreaker(String aName) {
        super(aName);
    }

    public SteamRockBreaker(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity arg0) {
        return new SteamRockBreaker(this.mName);
    }

    @Override
    public String getMachineType() {
        return "Rock Breaker";
    }

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final String STRUCTURE_PIECE_MAIN_SURVIVAL = "main_survival";

    private IStructureDefinition<SteamRockBreaker> STRUCTURE_DEFINITION = null;

    private static final int HORIZONTAL_OFF_SET = 5;
    private static final int VERTICAL_OFF_SET = 4;
    private static final int DEPTH_OFF_SET = 0;

    @Override
    public void onValueUpdate(byte aValue) {
        tierMachineCasing = aValue;
    }

    @Override
    public byte getUpdateData() {
        return (byte) tierMachineCasing;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        int id = tierMachine == 2 ? ((BlockCasings2) GregTechAPI.sBlockCasings2).getTextureIndex(0)
            : ((BlockCasings1) GregTechAPI.sBlockCasings1).getTextureIndex(10);
        if (side == aFacing) {
            if (aActive) return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(id), TextureFactory.builder()
                .addIcon(Textures.BlockIcons.OVERLAY_TOP_STEAM_MACERATOR_ACTIVE)
                .extFacing()
                .build() };
            return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(id), TextureFactory.builder()
                .addIcon(Textures.BlockIcons.OVERLAY_TOP_STEAM_MACERATOR)
                .extFacing()
                .build() };
        }
        return new ITexture[] { Textures.BlockIcons.getCasingTextureForId(id) };
    }

    private final String[][] shape = new String[][] {
        { "           ", "           ", "           ", "           ", "     E     ", "    EEE    ", "     E     ",
            "           ", "           ", "           ", "           " },
        { "           ", "           ", "           ", "           ", "     E     ", "    E E    ", "     E     ",
            "           ", "           ", "           ", "           " },
        { "           ", "   C   C   ", "   C   C   ", " CCD   DCC ", "     E     ", "    E E    ", "     E     ",
            " CCD   DCC ", "   C   C   ", "   C   C   ", "           " },
        { "   C   C   ", "           ", "           ", "C  D   D  C", "     E     ", "    E E    ", "     E     ",
            "C  D   D  C", "           ", "           ", "   C   C   " },
        { " CCCC~CCCC ", "CFFFFCFFFFC", "CFFFGEGFFFC", "CFFGEEEGFFC", "CFGEEEEEGFC", " CEEEEEEEC ", "CFGEEEEEGFC",
            "CFFGEEEGFFC", "CFFFGEGFFFC", "CFFFFCFFFFC", " CCCC CCCC " },
        { "CCCCCCCCCCC", "CAAAACAAAAC", "CACCCCCCCAC", "CACBBBBBCAC", "CACBBBBBCAC", "CCCBBBBBCCC", "CACBBBBBCAC",
            "CACBBBBBCAC", "CACCCCCCCAC", "CAAAACAAAAC", "CCCCCCCCCCC" } };

    @Override
    public IStructureDefinition<SteamRockBreaker> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<SteamRockBreaker>builder()
                .addShape(STRUCTURE_PIECE_MAIN, transpose(shape))
                .addShape(
                    STRUCTURE_PIECE_MAIN_SURVIVAL,
                    Arrays.stream(transpose(shape))
                        .map(
                            sa -> Arrays.stream(sa)
                                .map(s -> s.replaceAll("F", " "))
                                .map(s -> s.replaceAll("E", " "))
                                .toArray(String[]::new))
                        .toArray(String[][]::new))
                .addElement(
                    'C',
                    ofChain(
                        buildSteamInput(SteamRockBreaker.class).casingIndex(10)
                            .dot(1)
                            .build(),
                        buildHatchAdder(SteamRockBreaker.class)
                            .atLeast(SteamHatchElement.InputBus_Steam, SteamHatchElement.OutputBus_Steam)
                            .casingIndex(10)
                            .dot(1)
                            .buildAndChain(),
                        ofBlocksTiered(
                            SteamRockBreaker::getTierMachineCasing,
                            ImmutableList.of(Pair.of(sBlockCasings1, 10), Pair.of(sBlockCasings2, 0)),
                            -1,
                            (t, m) -> t.tierMachineCasing = m,
                            t -> t.tierMachineCasing)))
                .addElement(
                    'A',
                    ofBlocksTiered(
                        SteamRockBreaker::getTierPipeCasing,
                        ImmutableList.of(Pair.of(sBlockCasings2, 12), Pair.of(sBlockCasings2, 13)),
                        -1,
                        (t, m) -> t.tierPipeCasing = m,
                        t -> t.tierPipeCasing))
                .addElement('B', ofBlock(sBlockCasings4, 15))
                .addElement('D', ofBlock(Blocks.iron_block, 0))
                .addElement('F', ofAnyWater(true))
                .addElement('E', ofChain(ofBlockAnyMeta(Blocks.lava), ofBlockAnyMeta(Blocks.flowing_lava)))
                .addElement('G', ofBlock(Blocks.cobblestone, 0))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, HORIZONTAL_OFF_SET, VERTICAL_OFF_SET, DEPTH_OFF_SET);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {

        int built = survivialBuildPiece(
            STRUCTURE_PIECE_MAIN_SURVIVAL,
            stackSize,
            HORIZONTAL_OFF_SET,
            VERTICAL_OFF_SET,
            DEPTH_OFF_SET,
            elementBudget,
            env,
            true);
        if (built == -1) {
            GTUtility.sendChatToPlayer(
                env.getActor(),
                EnumChatFormatting.GOLD + "Auto placing done ! Now go place the water and lava by yourself !");
            return 0;
        }
        return built;
    }

    private int tierPipeCasing = -1;

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        tierPipeCasing = -1;
        tierMachineCasing = -1;
        tCountCasing = 0;
        if (!checkPiece(STRUCTURE_PIECE_MAIN, HORIZONTAL_OFF_SET, VERTICAL_OFF_SET, DEPTH_OFF_SET)) return false;
        if (tierPipeCasing < 0 && tierMachineCasing < 0) return false;
        if (tierPipeCasing == 1 && tierMachineCasing == 1 && tCountCasing >= 14 && checkHatches()) {
            updateHatchTexture();
            tierMachine = 1;
            return true;
        }
        if (tierPipeCasing == 2 && tierMachineCasing == 2 && tCountCasing >= 14 && checkHatches()) {
            updateHatchTexture();
            tierMachine = 2;
            return true;
        }
        return false;
    }

    @Override
    public int getMaxParallelRecipes() {
        return 8;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.rockBreakerFakeRecipes;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @Nonnull
            @Override
            protected CheckRecipeResult validateRecipe(@Nonnull GTRecipe recipe) {
                if (availableVoltage < recipe.mEUt) {
                    return CheckRecipeResultRegistry.insufficientPower(recipe.mEUt);
                }
                return CheckRecipeResultRegistry.SUCCESSFUL;
            }

            // note that a basic steam machine has .setEUtDiscount(2F).setSpeedBoost(2F). So these here are bonuses.
            @Override
            @Nonnull
            protected OverclockCalculator createOverclockCalculator(@NotNull GTRecipe recipe) {
                return OverclockCalculator.ofNoOverclock(recipe)
                    .setEUtDiscount(1.25 * tierMachine)
                    .setSpeedBoost(1.6 / tierMachine);
            }
        }.setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    @Override
    public int getTierRecipes() {
        return 1;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType(getMachineType())
            .addInfo("Works as a Rock Breaker, but big")
            .addInfo("Cog 1 = Cobblestone, Cog 2 = Stone, Cog 3 = Obsidian")
            .addInfo("Processes up to 8 recipes at once")
            .addInfo(
                EnumChatFormatting.AQUA + ""
                    + EnumChatFormatting.ITALIC
                    + "STOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONE")
            .addInfo(EnumChatFormatting.AQUA + "" + EnumChatFormatting.ITALIC + "Keep an eye for Wallsharing :>")
            .addInfo(HIGH_PRESSURE_TOOLTIP_NOTICE)
            .toolTipFinisher();
        return tt;
    }

    @Override
    public String[] getInfoData() {
        ArrayList<String> info = new ArrayList<>(Arrays.asList(super.getInfoData()));
        info.add("Machine Tier: " + EnumChatFormatting.YELLOW + tierMachine);
        return info.toArray(new String[0]);
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tag = accessor.getNBTData();
        currenttip.add(
            StatCollector.translateToLocal("GTPP.machines.tier") + ": "
                + EnumChatFormatting.YELLOW
                + getSteamTierTextForWaila(tag)
                + EnumChatFormatting.RESET);
        currenttip.add(
            StatCollector.translateToLocal("GT5U.multiblock.curparallelism") + ": "
                + EnumChatFormatting.BLUE
                + tag.getInteger("parallel")
                + EnumChatFormatting.RESET);
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setInteger("tierMachine", tierMachine);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("tierMachine", tierMachine);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        tierMachine = aNBT.getInteger("tierMachine");
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected SoundResource getActivitySoundLoop() {
        return SoundResource.IC2_MACHINES_ELECTROFURNACE_LOOP;
    }

}
