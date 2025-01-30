<?php declare(strict_types=1);
/*
 * PoiXson WebLinkMC - Link your website with your mc server
 * @copyright 2023-2025
 * @license AGPLv3+ADD-PXN-V1
 * @author lorenzo at poixson.com
 * @link https://poixson.com/
 */
namespace pxn\WebLinkMC\api;

use \pxn\phpUtils\Debug;


abstract class RequestArray implements RequestAPI {

	protected ?array $result = null;



	protected abstract function getRequestCommand(): string;



	public function getRequestJSON(): string {
		$json = [
			'request' => $this->getRequestCommand(),
		];
		$flags = (Debug::debug() ? \JSON_PRETTY_PRINT : 0);
		return \json_encode($json, $flags);
	}



	public function isDone(): bool {
		return ($this->result !== null);
	}



	public function process(string $data) {
		$json = \json_decode($data, true);
		$this->result = [ ];
		$key = $this->getRequestCommand();
		if (isset($json[$key]))
			$this->result = $json[$key];
	}



	public function getResult(): ?array {
		return $this->result;
	}



}
