package fr.leroideskiwis.kiwibot.commands;

import fr.leroideskiwis.kiwibot.Main;
import fr.leroideskiwis.kiwibot.command.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CommandGiveAway {

    @Command(name="ginfo",description = "Avoir les infos des giveaways")
    public void gInfo(TextChannel tx){

        tx.sendMessage("Règles : un membre aléatoirement choisi dans la liste des participants gagne à la fin de chaque partie. Vous pouvez voir la liste des participants en faisant ;gpart. Vous avez aussi plus de chances de gagner si vous invitez des gens (5 utilisations = +1 fois compté dans la liste).").queue();

    }

    @Command(name="gpart",description = "Voir la liste des participants")
    public void onGPart(JDA jda, String[] args, Guild guild, TextChannel channel, Main main, Member member){

        channel.sendMessage("Veuillez patienter... Recherche des participants...").queue();
        List<Member> members = getParticipants(main, guild, jda.getRoleById(main.getObs().concoursRole));

        EmbedBuilder builder = new EmbedBuilder().setColor(Color.CYAN);

        List<Member> memberst = new ArrayList<>();

        int number = 0;

        for(Member participant : members){

            int count = 0;

            if(memberst.contains(participant)) continue;

            for(Member member1 : members){

                if(member1.equals(participant)) {
                    count++;
                    continue;
                }

            }
            int chance = (int)((count/(double)members.size())*100.0);

            builder.addField(participant.getUser().getName(), chance+"% de chances de gagner !", true);


            memberst.add(participant);
            number++;

        }

        builder.setTitle("Il y a "+number+" participants.");

        channel.sendMessage(builder.build()).queue();
        channel.sendMessage("Faites "+main.getPrefixe()+"ggo confirm pour procéder au tirage au sort.").queue();

    }



    @Command(name="gregister", description = "s'incrire au concours")
    public void onRegister(Message msg, JDA jda, Guild guild, TextChannel channel, Main main, Member member){

        if(msg != null && member.equals(guild.getOwner()) && !msg.getMentionedMembers().isEmpty()){

            for(Member m : msg.getMentionedMembers()){

                onRegister(null, jda, guild, channel, main, m);

            }

        }

        if(member.getRoles().contains(jda.getRoleById(main.getObs().concoursRole))){

            channel.sendMessage("Erreur : vous êtes déjà inscrit !").queue();


        } else {
            guild.getController().addSingleRoleToMember(member, jda.getRoleById(main.getObs().concoursRole)).queue();

            channel.sendMessage("Vous êtes maintenant inscrit au concours !").queue();

        }


    }

    private List<Member> getParticipants(Main main, Guild guild, Role role){
        List<Member> members = new ArrayList<>();

        for(Member member : guild.getMembers()){

            for(Role roleM : member.getRoles()){

                if(roleM.equals(role)) {

                    Map<Member, Integer> invites = getInvites(guild);

                    if(invites.containsKey(member)) {

                        int uses = invites.get(member);

                        try {

                            members.add(member);

                            for (int i = 0; i < (uses / 5); i++) {

                                members.add(member);

                            }
                        }catch(Exception ignored){continue;}

                    } else members.add(member);

                    break;
                }

            }

        }

        return members;
    }

    public Map<Member, Integer> getInvites(Guild guild){

        Map<Member, Integer> returnV = new HashMap<>();

        for(Invite invite : guild.getInvites().complete()){

            try {

                Member m = guild.getMemberById(invite.getInviter().getId());

                int newI = 0;

                if (returnV.containsKey(m)){

                    newI = returnV.get(m)+invite.getUses();
                    returnV.remove(m);

                } else newI = invite.getUses();

                returnV.put(m, newI);

            }catch(Exception e){

                continue;

            }

        }

        return returnV;

    }

    @Command(name="ggo", description = "Tirer au sort ou voir le nombre de participants",op=true)
    public void onGo(JDA jda, String[] args, Guild guild, TextChannel channel, Main main, Member member){

            if(args.length != 0 && args[0].equalsIgnoreCase("confirm")) {

                channel.sendMessage("Veuillez patienter... Recherche des participants...").queue();
                List<Member> members = getParticipants(main, guild, jda.getRoleById(main.getObs().concoursRole));

                if (members.size() == 0) channel.sendMessage("Il n'y a aucun participant.").queue();
                else {

                    Member winner = members.get(new Random().nextInt(members.size()));
                    channel.sendMessage(winner.getUser().getName() + " a gagné ! Bravo à lui/elle !").queue();

                    for (Member member1 : members) {

                        guild.getController().removeSingleRoleFromMember(member1, jda.getRoleById(main.getObs().concoursRole)).queue();

                    }

                }
            } else channel.sendMessage("êtes-vous sûr de vouloir procéder au tirage au sort ? Faites ;ggo confirm si vous êtes sûr.").queue();



    }

}
