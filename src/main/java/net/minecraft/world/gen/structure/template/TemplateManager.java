package net.minecraft.world.gen.structure.template;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Map;

public class TemplateManager
{
    private final Map<String, Template> templates = Maps.<String, Template>newHashMap();
    private final String baseFolder;
    private final DataFixer fixer;

    public TemplateManager(String p_i47239_1_, DataFixer p_i47239_2_)
    {
        this.baseFolder = p_i47239_1_;
        this.fixer = p_i47239_2_;
    }

    public Template getTemplate(@Nullable MinecraftServer server, ResourceLocation id)
    {
        Template template = this.get(server, id);

        if (template == null)
        {
            template = new Template();
            this.templates.put(id.getResourcePath(), template);
        }

        return template;
    }

    @Nullable
    public Template get(@Nullable MinecraftServer server, ResourceLocation templatePath)
    {
        String s = templatePath.getResourcePath();

        if (this.templates.containsKey(s))
        {
            return this.templates.get(s);
        }
        else
        {
            if (server == null)
            {
                this.readTemplateFromJar(templatePath);
            }
            else
            {
                this.readTemplate(templatePath);
            }

            return this.templates.containsKey(s) ? (Template)this.templates.get(s) : null;
        }
    }

    public boolean readTemplate(ResourceLocation server)
    {
        String s = server.getResourcePath();
        File file1 = new File(this.baseFolder, s + ".nbt");

        if (!file1.exists())
        {
            return this.readTemplateFromJar(server);
        }
        else
        {
            InputStream inputstream = null;
            boolean flag;

            try
            {
                inputstream = new FileInputStream(file1);
                this.readTemplateFromStream(s, inputstream);
                return true;
            }
            catch (Throwable var10)
            {
                flag = false;
            }
            finally
            {
                IOUtils.closeQuietly(inputstream);
            }

            return flag;
        }
    }

    private boolean readTemplateFromJar(ResourceLocation id)
    {
        String s = id.getResourceDomain();
        String s1 = id.getResourcePath();
        InputStream inputstream = null;
        boolean flag;

        try
        {
            inputstream = MinecraftServer.class.getResourceAsStream("/assets/" + s + "/structures/" + s1 + ".nbt");
            this.readTemplateFromStream(s1, inputstream);
            return true;
        }
        catch (Throwable var10)
        {
            flag = false;
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }

        return flag;
    }

    private void readTemplateFromStream(String id, InputStream stream) throws IOException
    {
        NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(stream);

        if (!nbttagcompound.hasKey("DataVersion", 99))
        {
            nbttagcompound.setInteger("DataVersion", 500);
        }

        Template template = new Template();
        template.read(this.fixer.process(FixTypes.STRUCTURE, nbttagcompound));
        this.templates.put(id, template);
    }

    public boolean writeTemplate(@Nullable MinecraftServer server, ResourceLocation id)
    {
        String s = id.getResourcePath();

        if (server != null && this.templates.containsKey(s))
        {
            File file1 = new File(this.baseFolder);

            if (!file1.exists())
            {
                if (!file1.mkdirs())
                {
                    return false;
                }
            }
            else if (!file1.isDirectory())
            {
                return false;
            }

            File file2 = new File(file1, s + ".nbt");
            Template template = this.templates.get(s);
            OutputStream outputstream = null;
            boolean flag;

            try
            {
                NBTTagCompound nbttagcompound = template.writeToNBT(new NBTTagCompound());
                outputstream = new FileOutputStream(file2);
                CompressedStreamTools.writeCompressed(nbttagcompound, outputstream);
                return true;
            }
            catch (Throwable var13)
            {
                flag = false;
            }
            finally
            {
                IOUtils.closeQuietly(outputstream);
            }

            return flag;
        }
        else
        {
            return false;
        }
    }

    public void remove(ResourceLocation templatePath)
    {
        this.templates.remove(templatePath.getResourcePath());
    }
}