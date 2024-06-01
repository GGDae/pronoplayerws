package com.pronoplayer.app.discord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pronoplayer.app.AppApplication;
import com.pronoplayer.app.bo.Competition;
import com.pronoplayer.app.bo.DiscordChannel;
import com.pronoplayer.app.bo.DiscordConfig;
import com.pronoplayer.app.bo.Group;
import com.pronoplayer.app.bo.Traitor;
import com.pronoplayer.app.competition.CompetitionRepository;
import com.pronoplayer.app.config.SpringContext;
import com.pronoplayer.app.group.GroupRepository;
import com.pronoplayer.app.prono.PronoService;
import com.pronoplayer.app.user.UserRepository;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class DiscordListener extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        if (event.isFromGuild() && !event.getAuthor().getId().equals("245301536626835456")) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (content.startsWith("!pronoplayer remind")) {
            MessageChannel c = event.getChannel();
            String[] contents = content.split(" ");
            if (contents.length < 3) {
                c.sendMessage("Commande invalide").queue();
            } else {
                UserRepository userRepository = SpringContext.getBean(UserRepository.class);
                String userCode = contents[2];
                if (userCode.equals("stop")) {
                    Optional<com.pronoplayer.app.bo.User> userOpt = userRepository.findByDiscordId(event.getAuthor().getId());
                    if (userOpt.isPresent()) {
                        com.pronoplayer.app.bo.User user = userOpt.get();
                        user.getDiscord().setReminder(false);
                        c.sendMessage("Rappels désactivés.").queue();
                    }
                } else {
                    Optional<com.pronoplayer.app.bo.User> userOpt = userRepository.findByDiscordCode(userCode);
                    if (userOpt.isPresent()) {
                        com.pronoplayer.app.bo.User user = userOpt.get();
                        user.getDiscord().setId(event.getAuthor().getId());
                        user.getDiscord().setReminder(true);
                        userRepository.save(user);
                        c.sendMessage("Rappels activés en MP ! GL " + user.getLogin()).queue();
                    } else {
                        c.sendMessage("Code inconnu ! Copiez le code depuis votre profil sur https://pronoplayer.fr").queue();
                    }
                }
            }
        }
        if (content.startsWith("!pronoplayer")) {
            
            if (content.startsWith("!pronoplayer exposeTheTraitors")) {
                String[] contents = content.split(" ");
                MessageChannel c = event.getChannel();
                if (contents.length < 3) {
                    c.sendMessage("Manque des infos fréro, fais un effort").queue();
                } else {
                    String[] infos = contents[2].split("/");
                    if (infos.length < 5) {
                        c.sendMessage("Manque des infos fréro, fais un effort").queue();
                    } else {
                        exposeTheTraitors(infos[0], infos[1], infos[2], infos[3], infos[4], c);
                    }
                }
            }
            if (content.startsWith("!pronoplayer setup")) {
                GroupRepository groupRepository = SpringContext.getBean(GroupRepository.class);
                MessageChannel c = event.getChannel();
                String[] contents = content.split(" ");
                if (contents.length < 3) {
                    c.sendMessage("Données invalides").queue();
                } else {
                    Role role = null;
                    if (contents.length == 4) {
                        String roleToConfigure = contents[3].replace("_", " ");
                        List<Role> roles = event.getGuild().getRolesByName(roleToConfigure, true);
                        if (roles != null && !roles.isEmpty()) {
                            role = roles.get(0);
                        }
                    }
                    Group group = groupRepository.findById(contents[2]).orElse(null);
                    if (group != null) {
                        if (group.getDiscord() == null) {
                            group.setDiscord(new DiscordConfig());
                        }
                        if (group.getDiscord().getChannels() == null) {
                            group.getDiscord().setChannels(new ArrayList<>());
                        }
                        DiscordChannel localChannel = group.getDiscord().getChannels().stream().filter(ch -> ch.getChannel().equals(c.getId())).findFirst().orElse(null);
                        if (localChannel == null) {
                            localChannel = new DiscordChannel();
                            group.getDiscord().getChannels().add(localChannel);
                        }
                        localChannel.setChannel(c.getId());
                        if (role != null) {
                            localChannel.setRole(role.getId());
                        }
                        group.getDiscord().setEnabled(true);
                        groupRepository.save(group);
                        c.sendMessage("Groupe **" + group.getName() + "** initialisé dans le channel *" + c.getName() + "*").queue();
                    }
                }
            }
            if (content.equals("!pronoplayer stop")) {
                
                GroupRepository groupRepository = SpringContext.getBean(GroupRepository.class);
                MessageChannel c = event.getChannel();
                Group group = groupRepository.findByDiscordChannelsChannel(c.getId());
                if (group != null && group.getDiscord() != null && group.getDiscord().isEnabled()) {
                    group.getDiscord().getChannels().remove(group.getDiscord().getChannels().stream().filter(ch -> ch.getChannel().equals(c.getId())).findFirst().get());
                    if (group.getDiscord().getChannels().isEmpty()) {
                        group.getDiscord().setEnabled(false);
                    }
                    groupRepository.save(group);
                    c.sendMessage("Rappels désactivés pour le groupe *" + group.getName() + "*" + " sur ce channel").queue();
                }
            }
        }
    }
    
    public static void sendMessage(String message, String competitionId) {
        GroupRepository groupRepository = SpringContext.getBean(GroupRepository.class);
        List<Group> groups = groupRepository.findByDiscordEnabled(true);
        if (groups != null && !groups.isEmpty()) {
            for(Group group : groups) {
                if (group.getDiscord() != null && group.getDiscord().getCompetitions().contains(competitionId)) {
                    group.getDiscord().getChannels().forEach(channel -> {
                        TextChannel textChannel = AppApplication.jda.getTextChannelById(channel.getChannel());
                        MessageCreateBuilder builder = new MessageCreateBuilder();
                        if (channel.getRole() != null) {
                            Role role = AppApplication.jda.getRoleById(channel.getRole());
                            builder.addContent("<@&" + role.getId() + "> " + message);
                        } else {
                            builder.addContent(message);
                        }
                        builder.addContent("\nhttps://pronoplayer.fr/#/grp/" + group.getId() + "/" + competitionId);
                        textChannel.sendMessage(builder.build()).queue();
                    });
                }
            }
        }
    }
    
    public static void sendSnapshot(String groupId, String competitionId, InputStream inputStream) {
        GroupRepository groupRepository = SpringContext.getBean(GroupRepository.class);
        CompetitionRepository competitionRepository = SpringContext.getBean(CompetitionRepository.class);
        Group group = groupRepository.findById(groupId).orElse(null);
        Competition competition = competitionRepository.findById(competitionId).orElse(null);
        if (group != null && competition != null) {
            if (group.getDiscord() != null) {
                group.getDiscord().getChannels().forEach(channel -> {
                    TextChannel textChannel = AppApplication.jda.getTextChannelById(channel.getChannel());
                    MessageCreateBuilder builder = new MessageCreateBuilder();
                    String message  = group.getName() + " - Classement Provisoire - " + competition.getName() + " " + competition.getSplit();
                    builder.addContent(message);
                    textChannel.sendMessage(builder.build()).addFiles(FileUpload.fromData(inputStream, "snapshot.png")).queue();
                });
            }
        }
    }
    
    public static void sendStats(String groupId, String competitionId, InputStream inputStream, boolean isFuture) {
        GroupRepository groupRepository = SpringContext.getBean(GroupRepository.class);
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null && group.getDiscord() != null && group.getDiscord().getChannels() != null && group.getDiscord().getCompetitions().contains(competitionId)) {
            group.getDiscord().getChannels().forEach(channel -> {
                TextChannel textChannel = AppApplication.jda.getTextChannelById(channel.getChannel());
                MessageCreateBuilder builder = new MessageCreateBuilder();
                if (isFuture) {
                    builder.addContent("Le match va bientôt commencer !");
                } else {
                    builder.addContent("Le match commence !");
                }
                textChannel.sendMessage(builder.build()).addFiles(FileUpload.fromData(inputStream, "stats.png")).queue();
            });
        }
    }
    
    public static void sendPM(com.pronoplayer.app.bo.User user, String message) {
        User discordUser = AppApplication.jda.retrieveUserById(user.getDiscord().getId()).complete();
        discordUser.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();
    }
    
    public static void exposeTheTraitors(String group, String team, String name, String year, String split, MessageChannel c) {
        PronoService pronoService = SpringContext.getBean(PronoService.class);
        if (split.equals(".")) {
            split = "";
        }
        group = group.replace("_", " ");
        List<Traitor> traitors = pronoService.findTheTraitors(group, team, name, year, split);
        if (traitors != null) {
            MessageCreateBuilder builder = new MessageCreateBuilder();
            for (int i = 0; i < traitors.size(); i++) {
                builder.addContent("**" + traitors.get(i).getDisplayName() + "** : " + traitors.get(i).getNumberOfTreacheries() + " traîtrise(s) en " + traitors.get(i).getTotalMatchs() + " pronostic(s) \n");
                if (i > 0 && i % 10 == 0) {
                    c.sendMessage(builder.build()).queue();
                    builder.clear();
                }
            }
            c.sendMessage(builder.build()).queue();
        }
    }
}