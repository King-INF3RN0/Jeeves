package me.unrealization.jeeves.modules;

import java.util.HashMap;

import me.unrealization.jeeves.bot.Jeeves;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;

public class Internal implements BotModule
{
	private String version = Jeeves.version;
	private String[] commandList;
	private HashMap<String, Object> defaultConfig = new HashMap<String, Object>();

	public Internal()
	{
		this.commandList = new String[2];
		this.commandList[0] = "Version";
		this.commandList[1] = "Ping";
		this.defaultConfig.put("commandPrefix", "!");
		this.defaultConfig.put("respondOnPrefix", "0");
		this.defaultConfig.put("respondOnMention", "1");
		this.defaultConfig.put("ignoredChannels", new String[0]);
		this.defaultConfig.put("ignoredUsers", new String[0]);
	}

	@Override
	public HashMap<String, Object> getDefaultConfig()
	{
		return this.defaultConfig;
	}

	@Override
	public String getHelp()
	{
		return null;
	}

	@Override
	public String getVersion()
	{
		return this.version;
	}

	@Override
	public String[] getCommands()
	{
		return this.commandList;
	}

	@Override
	public String getDiscordId()
	{
		return "";
	}

	@Override
	public boolean canDisable()
	{
		return false;
	}

	public static class Ping implements BotCommand
	{
		@Override
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Permissions[] permissions()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean owner()
		{
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			Jeeves.sendMessage(message.getChannel(), "Pong!");
		}
	}

	public static class Version implements BotCommand
	{
		@Override
		public String help() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Permissions[] permissions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean owner() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments) {
			/*try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}*/

			Jeeves.sendMessage(message.getChannel(), Jeeves.version);
		}
	}
}
