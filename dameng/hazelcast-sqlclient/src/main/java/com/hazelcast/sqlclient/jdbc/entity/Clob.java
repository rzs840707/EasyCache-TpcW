package com.hazelcast.sqlclient.jdbc.entity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import java.sql.SQLException;

import com.mysql.jdbc.StringUtils;

/**
 * Simplistic implementation of java.sql.Clob for MySQL Connector/J
 * 
 * @author Mark Matthews
 * @version $Id: Clob.java 5417 2006-06-20 21:33:56Z mmatthews $
 */
public class Clob implements java.sql.Clob, OutputStreamWatcher, WriterWatcher {
	private String charData;

	public Clob(String charDataInit) {
		this.charData = charDataInit;
	}

	/**
	 * @see java.sql.Clob#getAsciiStream()
	 */
	public InputStream getAsciiStream() throws SQLException {
		if (this.charData != null) {
			return new ByteArrayInputStream(this.charData.getBytes());
		}

		return null;
	}

	/**
	 * @see java.sql.Clob#getCharacterStream()
	 */
	public Reader getCharacterStream() throws SQLException {
		if (this.charData != null) {
			return new StringReader(this.charData);
		}

		return null;
	}

	/**
	 * @see java.sql.Clob#getSubString(long, int)
	 */
	public String getSubString(long startPos, int length) throws SQLException {
		if (startPos < 1) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		int adjustedStartPos = (int)startPos - 1;
		int adjustedEndIndex = adjustedStartPos + length;
		
		if (this.charData != null) {
			if (adjustedEndIndex > this.charData.length()) {
				throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
			}

			return this.charData.substring(adjustedStartPos, 
					adjustedEndIndex);
		}

		return null;
	}

	/**
	 * @see java.sql.Clob#length()
	 */
	public long length() throws SQLException {
		if (this.charData != null) {
			return this.charData.length();
		}
		return 0;
	}

	/**
	 * @see java.sql.Clob#position(Clob, long)
	 */
	public long position(java.sql.Clob arg0, long arg1) throws SQLException {
		return position(arg0.getSubString(0L, (int) arg0.length()), arg1);
	}

	/**
	 * @see java.sql.Clob#position(String, long)
	 */
	public long position(String stringToFind, long startPos)
			throws SQLException {
		if (startPos < 1) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		if (this.charData != null) {
			if ((startPos - 1) > this.charData.length()) {
				throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
			}

			int pos = this.charData.indexOf(stringToFind, (int) (startPos - 1));

			return (pos == -1) ? (-1) : (pos + 1);
		}

		return -1;
	}

	/**
	 * @see java.sql.Clob#setAsciiStream(long)
	 */
	public OutputStream setAsciiStream(long indexToWriteAt) throws SQLException {
		if (indexToWriteAt < 1) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		WatchableOutputStream bytesOut = new WatchableOutputStream();
		bytesOut.setWatcher(this);

		if (indexToWriteAt > 0) {
			bytesOut.write(this.charData.getBytes(), 0,
					(int) (indexToWriteAt - 1));
		}

		return bytesOut;
	}

	/**
	 * @see java.sql.Clob#setCharacterStream(long)
	 */
	public Writer setCharacterStream(long indexToWriteAt) throws SQLException {
		if (indexToWriteAt < 1) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		WatchableWriter writer = new WatchableWriter();
		writer.setWatcher(this);

		//
		// Don't call write() if nothing to write...
		//
		if (indexToWriteAt > 1) {
			writer.write(this.charData, 0, (int) (indexToWriteAt - 1));
		}

		return writer;
	}

	/**
	 * @see java.sql.Clob#setString(long, String)
	 */
	public int setString(long pos, String str) throws SQLException {
		if (pos < 1) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		if (str == null) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		StringBuffer charBuf = new StringBuffer(this.charData);

		pos--;

		int strLength = str.length();

		charBuf.replace((int) pos, (int) (pos + strLength), str);

		this.charData = charBuf.toString();

		return strLength;
	}

	/**
	 * @see java.sql.Clob#setString(long, String, int, int)
	 */
	public int setString(long pos, String str, int offset, int len)
			throws SQLException {
		if (pos < 1) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		if (str == null) {
			throw new SQLException("SQL_STATE_ILLEGAL_ARGUMENT");
		}

		StringBuffer charBuf = new StringBuffer(this.charData);

		pos--;

		String replaceString = str.substring(offset, len);

		charBuf.replace((int) pos, (int) (pos + replaceString.length()),
				replaceString);

		this.charData = charBuf.toString();

		return len;
	}

	/**
	 * @see com.mysql.jdbc.OutputStreamWatcher#streamClosed(byte[])
	 */
	public void streamClosed(WatchableOutputStream out) {
		int streamSize = out.size();

		if (streamSize < this.charData.length()) {
			try {
				out.write(StringUtils
						.getBytes(this.charData, null, null, false, null),
						streamSize, this.charData.length() - streamSize);
			} catch (SQLException ex) {
				//
			}
		}

		this.charData = StringUtils.toAsciiString(out.toByteArray());
	}

	/**
	 * @see java.sql.Clob#truncate(long)
	 */
	public void truncate(long length) throws SQLException {
		if (length > this.charData.length()) {
			throw new SQLException("truncate exception");
		}

		this.charData = this.charData.substring(0, (int) length);
	}

	/**
	 * @see com.mysql.jdbc.WriterWatcher#writerClosed(char[])
	 */
	public void writerClosed(char[] charDataBeingWritten) {
		this.charData = new String(charDataBeingWritten);
	}

	/**
	 * @see com.mysql.jdbc.WriterWatcher#writerClosed(char[])
	 */
	public void writerClosed(WatchableWriter out) {
		int dataLength = out.size();

		if (dataLength < this.charData.length()) {
			out.write(this.charData, dataLength, this.charData.length()
					- dataLength);
		}

		this.charData = out.toString();
	}

	public void free() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}