package me.unrealization.jeeves.bot;

import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.NewUserHandler;
import me.unrealization.jeeves.interfaces.PresenceUpdateHandler;
import me.unrealization.jeeves.interfaces.UserUpdateHandler;

import java.util.EnumSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.modules.Internal;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MentionEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserUpdateEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;

public class DiscordEventHandlers
{
	public static class ReadyEventListener implements IListener<ReadyEvent>
	{
		@Override
		public void handle(ReadyEvent event)
		{
			IDiscordClient bot = event.getClient();

			EventDispatcher dispatcher = bot.getDispatcher();
			dispatcher.registerListener(new MessageReceivedListener());
			dispatcher.registerListener(new MentionListener());
			dispatcher.registerListener(new NewUserListener());
			dispatcher.registerListener(new UserUpdateListener());
			dispatcher.registerListener(new UserPresenceListener());
			dispatcher.registerListener(new GuildCreateListener());

			IUser botUser = bot.getOurUser();
			System.out.println("Logged in as " + botUser.getName() + " (" + Jeeves.version + ")");
		}
	}

	public static class MessageReceivedListener implements IListener<MessageReceivedEvent>
	{
		@Override
		public void handle(MessageReceivedEvent event)
		{
			IMessage message = event.getMessage();
			String messageContent = message.getContent();

			if (messageContent.startsWith((String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix")) == true)
			{
				DiscordEventHandlers.handleMessage(message);
			}
		}
	}

	public static class MentionListener implements IListener<MentionEvent>
	{
		@Override
		public void handle(MentionEvent event)
		{
			IMessage message = event.getMessage();
			String messageContent = message.getContent();
			IUser botUser = event.getClient().getOurUser();

			if ((messageContent.startsWith(botUser.mention(true)) == true) || (messageContent.startsWith(botUser.mention(false)) == true))
			{
				DiscordEventHandlers.handleMessage(message);
			}
		}
	}

	public static class NewUserListener implements IListener<UserJoinEvent>
	{
		@Override
		public void handle(UserJoinEvent event)
		{
			Set<String> moduleSet = Jeeves.modules.keySet();
			String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				NewUserHandler module;

				try
				{
					module = (NewUserHandler)Jeeves.modules.get(moduleList[moduleIndex]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}

				module.newUserHandler(event);
			}
		}
	}

	public static class UserUpdateListener implements IListener<UserUpdateEvent>
	{
		@Override
		public void handle(UserUpdateEvent event)
		{
			Set<String> moduleSet = Jeeves.modules.keySet();
			String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				UserUpdateHandler module;

				try
				{
					module = (UserUpdateHandler)Jeeves.modules.get(moduleList[moduleIndex]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}

				module.userUpdateHandler(event);
			}
		}
	}

	public static class UserPresenceListener implements IListener<PresenceUpdateEvent>
	{
		@Override
		public void handle(PresenceUpdateEvent event)
		{
			Set<String> moduleSet = Jeeves.modules.keySet();
			String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				PresenceUpdateHandler module;

				try
				{
					module = (PresenceUpdateHandler)Jeeves.modules.get(moduleList[moduleIndex]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}

				module.presenceUpdateHandler(event);
			}
		}
	}

	public static class GuildCreateListener implements IListener<GuildCreateEvent>
	{
		@Override
		public void handle(GuildCreateEvent event)
		{
			System.out.println("Creating default config for " + event.getGuild().getName());
			Internal internal = new Internal();

			try
			{
				Jeeves.checkConfig(event.getGuild().getID(), internal.getDefaultConfig());
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				e.printStackTrace();
				System.out.println("Cannot create default config for " + event.getGuild().getName());
			}
		}
	}

	private static void handleMessage(IMessage message)
	{
		String messageContent = message.getContent();
		IUser botUser = message.getClient().getOurUser();
		int cutLength = 0;
		String commandPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix");

		if (messageContent.startsWith(commandPrefix))
		{
			cutLength = commandPrefix.length();
		}
		else if (messageContent.startsWith(botUser.mention(true)))
		{
			cutLength = botUser.mention(true).length();
		}
		else if (messageContent.startsWith(botUser.mention(false)))
		{
			cutLength = botUser.mention(false).length();
		}
		else
		{
			System.out.println("What is this message doing here?");
			System.out.println("Message: " + message.getContent());
		}
		
		if (cutLength > 0)
		{
			messageContent = messageContent.substring(cutLength);
		}

		String[] messageParts = messageContent.split(" ");

		while ((messageParts.length > 0) && (messageParts[0].length() == 0))
		{
			String[] tmpParts = new String[messageParts.length - 1];

			for (int x = 1; x < messageParts.length; x++)
			{
				tmpParts[x - 1] = messageParts[x];
			}
			
			messageParts = tmpParts;
		}

		if (messageParts.length == 0)
		{
			System.out.println("Empty");
			return;
		}

		String commandName = messageParts[0].toLowerCase();
		String[] arguments = new String[messageParts.length - 1];

		for (int x = 1; x < messageParts.length; x++)
		{
			arguments[x - 1] = messageParts[x];
		}

		Set<String> moduleSet = Jeeves.modules.keySet();
		String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

		for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
		{
			BotModule module = Jeeves.modules.get(moduleList[moduleIndex]);
			String[] commandList = module.getCommands();

			for (int commandIndex = 0; commandIndex < commandList.length; commandIndex++)
			{
				if (commandList[commandIndex].toLowerCase().equals(commandName) == false)
				{
					continue;
				}

				try
				{
					Jeeves.checkConfig(message.getGuild().getID(), module.getDefaultConfig());
				}
				catch (ParserConfigurationException | TransformerException e)
				{
					e.printStackTrace();
					return;
				}

				String discordId = module.getDiscordId();

				if ((discordId != null) && (discordId != message.getGuild().getID()))
				{
					Jeeves.sendMessage(message.getChannel(), "This command is not available on this Discord.");
					return;
				}

				Class<?> commandClass;

				try
				{
					commandClass = Class.forName(module.getClass().getName() + "$" + commandList[commandIndex]);
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
					return;
				}

				BotCommand command;

				try
				{
					command = (BotCommand)commandClass.newInstance();
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					e.printStackTrace();
					return;
				}

				if (command == null)
				{
					System.out.println("Error");
				}

				if (command.owner() == true)
				{
					String ownerId = "";

					try
					{
						ownerId = message.getClient().getApplicationOwner().getID();
					}
					catch (DiscordException e)
					{
						e.printStackTrace();
					}

					//TODO: Test
					//if (message.getAuthor().getID().equals(ownerId) == false)
					if (message.getAuthor().getID().equals(ownerId) == true)
					{
						Jeeves.sendMessage(message.getChannel(), "You are not permitted to execute this command.");
						return;
					}
				}

				Permissions[] permissionList = command.permissions();

				if (permissionList != null)
				{
					EnumSet<Permissions> userPermissions = message.getAuthor().getPermissionsForGuild(message.getGuild());

					for (int permissionIndex = 0; permissionIndex < permissionList.length; permissionIndex++)
					{
						if (userPermissions.contains(permissionList[permissionIndex]) == false)
						{
							Jeeves.sendMessage(message.getChannel(), "You are not permitted to execute this command.");
							return;
						}
					}
				}

				System.out.println("Executing " + commandName + " for " + message.getAuthor().getName() + " (" + message.getGuild().getName() + ")");
				command.execute(message, arguments);
				//TaskHandler executor = new TaskHandler(message, command, arguments);
				//executor.start();
			}
		}
	}
}
