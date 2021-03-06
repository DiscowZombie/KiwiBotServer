package fr.leroideskiwis.kiwibot.commands;

import fr.leroideskiwis.kiwibot.Main;
import fr.leroideskiwis.kiwibot.command.Command;
import fr.leroideskiwis.kiwibot.command.CommandCore;
import fr.leroideskiwis.kiwibot.command.SimpleCommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.io.PrintStream;

public class BasicCommands {

    @Command(name="stop",type= Command.ExecutorType.ALL,op = true)
    public void stop(Main main, PrintStream printStream){

        printStream.println("Le bot s'est arrêté !");
        main.setRunning(false);

    }

    @Command(name="afk",description="se mettre afk")
    public void afk(Member member, JDA jda, Main main, Guild guild, TextChannel channel){

        if(member.getRoles().contains(jda.getRolesByName("afk", true).get(0))){

            guild.getController().removeSingleRoleFromMember(member, jda.getRolesByName("afk", true).get(0)).complete();

            channel.sendMessage("Vous n'êtes désormais plus afk !").queue();

        } else {

            guild.getController().addSingleRoleToMember(member, jda.getRolesByName("afk", true).get(0)).complete();
            channel.sendMessage("Vous êtes désormais afk !").queue();

        }

    }

    @Command(name="github",description = "Avoir accès au channel github")
    public void github(JDA jda, Member member, TextChannel channel, Guild guild, Main main){

        if(member.getRoles().contains(jda.getRoleById(main.getConfig("githubRole")))){

            guild.getController().removeSingleRoleFromMember(member, jda.getRoleById(main.getConfig("githubRole"))).complete();

            channel.sendMessage("Vous n'avez désormais plus accès aux channels github !").queue();

        } else {

            guild.getController().addSingleRoleToMember(member, jda.getRoleById(main.getConfig("githubRole"))).complete();
            channel.sendMessage("Vous avez désormais accès aux channels github !").queue();

        }

    }

    @Command(name="purge",op=true)
    public void onPurge(String[] args, TextChannel channel, Member member){

        channel.getHistory().retrievePast(Integer.parseInt(args[0])).complete().forEach(m -> m.delete().complete());

        channel.sendMessage(Integer.parseInt(args[0])+" messages ont été supprimés !").queue();

    }

    @Command(name="help")
    public void onHelp(Main main, Guild guild, Member member, TextChannel channel, CommandCore core){

        EmbedBuilder builder = new EmbedBuilder().setColor(Color.CYAN);
        EmbedBuilder builderO = new EmbedBuilder().setColor(Color.RED);

        int count = 0;
        int countO = 0;

        for(SimpleCommand command : core.getCommands()){

            if(!command.needOp()){

                    builder.addField(command.getName(), command.getDescription(), false);

                    count++;

                continue;
            } else {
                builderO.addField(command.getName(), command.getDescription(), false);
                countO++;
            }

        }

        builder.setTitle(count+" commandes ; préfixe : "+main.getPrefixe());
        builderO.setTitle(countO+" commandes reservé au propriétaire du serveur");

        channel.sendMessage(builder.build()).queue();
        if(member.equals(guild.getOwner())) channel.sendMessage(builderO.build()).queue();

    }

}
