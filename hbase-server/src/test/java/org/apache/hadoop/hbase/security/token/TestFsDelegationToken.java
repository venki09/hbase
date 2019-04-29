package org.apache.hadoop.hbase.security.token;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.UserProvider;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.hdfs.web.SWebHdfsFileSystem;
import org.apache.hadoop.hdfs.web.WebHdfsFileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.token.Token;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestFsDelegationToken {
  private UserProvider userProvider = Mockito.mock(UserProvider.class);
  private User user = Mockito.mock(User.class);
  private FsDelegationToken fsDelegationToken = new FsDelegationToken(userProvider, "renewer");
  private Token hdfsToken = Mockito.mock(Token.class);
  private Token webhdfsToken = Mockito.mock(Token.class);
  private Token swebhdfsToken = Mockito.mock(Token.class);
  private WebHdfsFileSystem webHdfsFileSystem = Mockito.mock(WebHdfsFileSystem.class);
  private WebHdfsFileSystem swebHdfsFileSystem = Mockito.mock(SWebHdfsFileSystem.class);
  private FileSystem fileSystem = Mockito.mock(FileSystem.class);

  @Before
  public void setup() throws IOException, URISyntaxException {
    when(userProvider.getCurrent()).thenReturn(user);
    when(userProvider.isHadoopSecurityEnabled()).thenReturn(true);
    when(fileSystem.getCanonicalServiceName()).thenReturn("hdfs://");
    when(fileSystem.getUri()).thenReturn(new URI("hdfs://someUri"));
    when(webHdfsFileSystem.getCanonicalServiceName()).thenReturn("webhdfs://");
    when(webHdfsFileSystem.getUri()).thenReturn(new URI("webhdfs://someUri"));
    when(swebHdfsFileSystem.getCanonicalServiceName()).thenReturn("swebhdfs://");
    when(swebHdfsFileSystem.getUri()).thenReturn(new URI("swebhdfs://someUri"));
    when(user.getToken(DelegationTokenIdentifier.HDFS_DELEGATION_KIND.toString(),
        fileSystem.getCanonicalServiceName()))
        .thenReturn(hdfsToken);
    when(user.getToken(
        WebHdfsFileSystem.TOKEN_KIND.toString(),
        webHdfsFileSystem.getCanonicalServiceName())).thenReturn(webhdfsToken);
    when(user.getToken(
        SWebHdfsFileSystem.TOKEN_KIND.toString(),
        swebHdfsFileSystem.getCanonicalServiceName())).thenReturn(swebhdfsToken);
    when(hdfsToken.getKind()).thenReturn(new Text("HDFS_DELEGATION_TOKEN"));
    when(webhdfsToken.getKind()).thenReturn(WebHdfsFileSystem.TOKEN_KIND);
    when(swebhdfsToken.getKind()).thenReturn(SWebHdfsFileSystem.TOKEN_KIND);
  }

  @Test
  public void acquireDelegationToken_defaults_to_hdfsFileSystem() throws IOException {
    fsDelegationToken.acquireDelegationToken(fileSystem);
    assertEquals(
        fsDelegationToken.getUserToken().getKind(), DelegationTokenIdentifier.HDFS_DELEGATION_KIND);
  }

  @Test
  public void acquireDelegationToken_webhdfsFileSystem() throws IOException {
    fsDelegationToken.acquireDelegationToken(webHdfsFileSystem);
    assertEquals(
        fsDelegationToken.getUserToken().getKind(), WebHdfsFileSystem.TOKEN_KIND);
  }

  @Test
  public void acquireDelegationToken_swebhdfsFileSystem() throws IOException {
    fsDelegationToken.acquireDelegationToken(swebHdfsFileSystem);
    assertEquals(
        fsDelegationToken.getUserToken().getKind(), SWebHdfsFileSystem.TOKEN_KIND);
  }

  @Test(expected = NullPointerException.class)
  public void acquireDelegationTokenByTokenKind_rejects_null_token_kind() throws IOException {
    fsDelegationToken.acquireDelegationToken(null, fileSystem);
  }

  @Test
  public void acquireDelegationTokenByTokenKind_webhdfsFileSystem() throws IOException {
    fsDelegationToken
        .acquireDelegationToken(WebHdfsFileSystem.TOKEN_KIND.toString(), webHdfsFileSystem);
    assertEquals(fsDelegationToken.getUserToken().getKind(), WebHdfsFileSystem.TOKEN_KIND);
  }

  @Test
  public void acquireDelegationTokenByTokenKind_swebhdfsFileSystem() throws IOException {
    fsDelegationToken
        .acquireDelegationToken(SWebHdfsFileSystem.TOKEN_KIND.toString(), swebHdfsFileSystem);
    assertEquals(fsDelegationToken.getUserToken().getKind(), SWebHdfsFileSystem.TOKEN_KIND);
  }
}
